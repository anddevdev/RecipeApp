package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.api.RecipeApiService
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.data.toRecipe
import com.example.recipeapp.repositories.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val recipeApiService: RecipeApiService,
    private val recipeDetailsApiService: RecipeDetailsApiService
) : ViewModel() {

    private val _recommendedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recommendedRecipes: StateFlow<List<Recipe>> get() = _recommendedRecipes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _showingDefaultRecommendations = MutableStateFlow(false)
    val showingDefaultRecommendations: StateFlow<Boolean> get() = _showingDefaultRecommendations

    private var cachedFavoriteRecipes: List<Recipe> = emptyList()
    private var cachedPreferences: UserPreferences? = null
    private var cachedRecommendations: List<Recipe> = emptyList()


    fun fetchRecommendedRecipes() {
        viewModelScope.launch {
            _loading.value = true  // Start loading
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("RecommendationsViewModel", "User ID: $userId")

            if (userId != null) {
                val favoriteRecipes = firestoreRepository.getFavoriteRecipesWithDetails(userId)
                Log.d("RecommendationsViewModel", "Favorite Recipes: $favoriteRecipes")

                // Do not re-analyze the recipes each time user enters this screen.
                if (cachedFavoriteRecipes.isEmpty() || favoriteRecipes != cachedFavoriteRecipes) {
                    cachedFavoriteRecipes = favoriteRecipes

                    if (favoriteRecipes.isNotEmpty()) {
                        _showingDefaultRecommendations.value = false
                        val preferences = analyzePreferences(favoriteRecipes)
                        cachedPreferences = preferences
                        Log.d("RecommendationsViewModel", "User Preferences: $preferences")

                        //Limit the number of displayed recommended recipes and enhance recommendation logic.
                        val recommendations = getRecommendations(preferences, favoriteRecipes)
                        cachedRecommendations = recommendations
                        Log.d("RecommendationsViewModel", "Recommendations: $recommendations")

                        _recommendedRecipes.value = recommendations
                    } else {
                        _showingDefaultRecommendations.value = true
                        val defaultRecommendations = getDefaultRecommendations()
                        cachedRecommendations = defaultRecommendations
                        Log.d("RecommendationsViewModel", "Default Recommendations: $defaultRecommendations")
                        _recommendedRecipes.value = defaultRecommendations
                    }
                } else {
                    // Favorites haven't changed, use cached recommendations
                    Log.d("RecommendationsViewModel", "Using cached recommendations")
                    _recommendedRecipes.value = cachedRecommendations
                }
            } else {
                Log.e("RecommendationsViewModel", "User ID is null")
            }
            _loading.value = false  // Stop loading
        }
    }

    private suspend fun getRecommendations(
        preferences: UserPreferences,
        favoriteRecipes: List<Recipe>,
        maxRecommendations: Int = 10 //Limit the number of displayed recommended recipes
    ): List<Recipe> {
        val recommendedRecipes = mutableMapOf<Recipe, Int>()
        val favoriteRecipeIds = favoriteRecipes.map { it.idMeal }.toSet()

        val allPotentialRecipes = mutableMapOf<String, RecipeDetails>() // Map idMeal to RecipeDetails

        // Enhance the recommendation logic by fetching recipes based on both favorite categories and ingredients.
        // Fetch recipes by favorite categories
        for (category in preferences.favoriteCategories.take(3)) {
            val response = recipeApiService.getRecipesByCategory(category)
            response.meals.let { meals ->
                meals.forEach { meal ->
                    if (meal.idMeal !in favoriteRecipeIds && meal.idMeal !in allPotentialRecipes.keys) {
                        val recipeDetailResponse = recipeDetailsApiService.getRecipeById(meal.idMeal)
                        val detailedRecipe = recipeDetailResponse?.meals?.firstOrNull()
                        detailedRecipe?.let { recipeDetails ->
                            allPotentialRecipes[meal.idMeal] = recipeDetails
                        }
                    }
                }
            }
        }

        // Fetch recipes by favorite ingredients
        for (ingredient in preferences.favoriteIngredients.take(5)) {
            val response = recipeApiService.getRecipesByIngredient(ingredient)
            response.meals.let { meals ->
                meals.forEach { meal ->
                    if (meal.idMeal !in favoriteRecipeIds && meal.idMeal !in allPotentialRecipes.keys) {
                        val recipeDetailResponse = recipeDetailsApiService.getRecipeById(meal.idMeal)
                        val detailedRecipe = recipeDetailResponse?.meals?.firstOrNull()
                        detailedRecipe?.let { recipeDetails ->
                            allPotentialRecipes[meal.idMeal] = recipeDetails
                        }
                    }
                }
            }
        }

        // Score and sort recipes
        for ((_, recipeDetails) in allPotentialRecipes) {
            val score = calculateRecipeScore(recipeDetails, preferences)
            val recipe = recipeDetails.toRecipe()
            recommendedRecipes[recipe] = score
        }

        val sortedRecipes = recommendedRecipes.entries.sortedByDescending { it.value }.map { it.key }
        return sortedRecipes.take(maxRecommendations) // Limit to maxRecommendations
    }

    private fun calculateRecipeScore(recipeDetails: RecipeDetails, preferences: UserPreferences): Int {
        var score = 0

        // Increase score if recipe's category is a favorite
        if (recipeDetails.strCategory in preferences.favoriteCategories) {
            score += 5
        }

        // Increase score based on matching ingredients
        val ingredients = extractIngredients(recipeDetails)
        val matchingIngredients = ingredients.intersect(preferences.favoriteIngredients.toSet())
        score += matchingIngredients.size * 2

        return score
    }

    private suspend fun analyzePreferences(favoriteRecipes: List<Recipe>): UserPreferences {
        val categoryCounts = mutableMapOf<String, Int>()
        val ingredientCounts = mutableMapOf<String, Int>()

        for (recipe in favoriteRecipes) {
            // Count categories
            recipe.strCategory?.let { category ->
                categoryCounts[category] = categoryCounts.getOrDefault(category, 0) + 1
            }

            // Fetch recipe details to get ingredients
            val recipeDetails = getRecipeDetailsById(recipe.idMeal)
            val ingredients = extractIngredients(recipeDetails)
            for (ingredient in ingredients) {
                ingredientCounts[ingredient] = ingredientCounts.getOrDefault(ingredient, 0) + 1
            }
        }

        // Determine top categories and ingredients
        val topCategories = categoryCounts.toList().sortedByDescending { it.second }.map { it.first }
        val topIngredients = ingredientCounts.toList().sortedByDescending { it.second }.map { it.first }

        return UserPreferences(
            favoriteCategories = topCategories,
            favoriteIngredients = topIngredients
        )
    }

    private suspend fun getRecipeDetailsById(idMeal: String): RecipeDetails? {
        val response = recipeDetailsApiService.getRecipeById(idMeal)
        return response?.meals?.firstOrNull()
    }

    private fun extractIngredients(recipeDetails: RecipeDetails?): List<String> {
        val ingredients = mutableListOf<String>()
        if (recipeDetails != null) {
            // Access strIngredient1 to strIngredient20 from RecipeDetails
            val clazz = recipeDetails::class
            for (i in 1..20) {
                val propertyName = "strIngredient$i"
                val property = clazz.members.find { it.name == propertyName } as? kotlin.reflect.KProperty1<RecipeDetails, String?>
                val value = property?.get(recipeDetails)
                if (!value.isNullOrBlank()) {
                    ingredients.add(value)
                }
            }
        }
        return ingredients
    }

    private suspend fun getDefaultRecommendations(maxRecommendations: Int = 10): List<Recipe> {
        // Fetch some default recipes
        val response = recipeApiService.getRecipesByCategory("Seafood") // Example category
        val recipes = mutableListOf<Recipe>()
        val meals = response.meals
        for (meal in meals) {
            val recipeDetailResponse = recipeDetailsApiService.getRecipeById(meal.idMeal)
            val detailedRecipe = recipeDetailResponse?.meals?.firstOrNull()
            if (detailedRecipe != null) {
                val recipe = detailedRecipe.toRecipe()
                recipes.add(recipe)
                if (recipes.size >= maxRecommendations) break
            }
            if (recipes.size >= maxRecommendations) {
                break
            }
        }
        return recipes
    }
}

data class UserPreferences(
    val favoriteCategories: List<String>,
    val favoriteIngredients: List<String>
)

//TODO: 1. Do not re-analyze the recipes each time user enters this screen. 2. Limit the number of displayed recommended recipes. 3. Enhance the recommendation logic. Do not just give all the meals from favorited categories. Enhance the recommendations somehow.

