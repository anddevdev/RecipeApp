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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationsViewModel : ViewModel() {

    private val firestoreRepository: FirestoreRepository
    private val recipeApiService: RecipeApiService = RecipeApiService.instance
    private val recipeDetailsApiService: RecipeDetailsApiService = RecipeDetailsApiService.instance

    private val _recommendedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recommendedRecipes: StateFlow<List<Recipe>> get() = _recommendedRecipes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    init {
        firestoreRepository = FirestoreRepository(recipeDetailsApiService)
    }

    fun fetchRecommendedRecipes() {
        viewModelScope.launch {
            _loading.value = true  // Start loading
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("RecommendationsViewModel", "User ID: $userId")

            if (userId != null) {
                val favoriteRecipes = firestoreRepository.getFavoriteRecipesWithDetails(userId)
                Log.d("RecommendationsViewModel", "Favorite Recipes: $favoriteRecipes")

                if (favoriteRecipes.isNotEmpty()) {
                    val preferences = analyzePreferences(favoriteRecipes)
                    Log.d("RecommendationsViewModel", "User Preferences: $preferences")

                    val recommendations = getRecommendations(preferences, favoriteRecipes)
                    Log.d("RecommendationsViewModel", "Recommendations: $recommendations")

                    _recommendedRecipes.value = recommendations
                } else {
                    val defaultRecommendations = getDefaultRecommendations()
                    Log.d("RecommendationsViewModel", "Default Recommendations: $defaultRecommendations")
                    _recommendedRecipes.value = defaultRecommendations
                }
            } else {
                Log.e("RecommendationsViewModel", "User ID is null")
            }
            _loading.value = false  // Stop loading
        }
    }

    private suspend fun getRecommendations(
        preferences: UserPreferences,
        favoriteRecipes: List<Recipe>
    ): List<Recipe> {
        val recommendedRecipes = mutableSetOf<Recipe>()
        val favoriteRecipeNames = favoriteRecipes.map { it.strMeal }

        // Fetch recipes by top categories
        for (category in preferences.favoriteCategories.take(3)) {
            val response = recipeApiService.getRecipesByCategory(category)
            response.meals.let { meals ->
                meals.forEach { meal ->
                    if (meal.strMeal !in favoriteRecipeNames) {
                        val recipeDetailResponse = recipeDetailsApiService.getRecipeByName(meal.strMeal)
                        val matchingRecipes = recipeDetailResponse.meals.filter { it.strMeal == meal.strMeal }
                        val detailedRecipe = matchingRecipes.firstOrNull()
                        detailedRecipe?.let { recipeDetails ->
                            val recipe = recipeDetails.toRecipe()
                            recommendedRecipes.add(recipe)
                        }
                    }
                }
            }
        }

        return recommendedRecipes.toList()
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
            // If ingredients are not available in Recipe, fetch RecipeDetails
            // Assuming you have a method to fetch RecipeDetails by idMeal
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

    private suspend fun getDefaultRecommendations(): List<Recipe> {
        // Fetch some default recipes, perhaps the most popular ones
        val response = recipeApiService.getRecipesByCategory("Seafood") // Example category
        val recipes = mutableListOf<Recipe>()
        response.meals.let { meals ->
            meals.forEach { meal ->
                val recipeDetailResponse = recipeDetailsApiService.getRecipeById(meal.idMeal)
                val detailedRecipe = recipeDetailResponse?.meals?.firstOrNull()
                detailedRecipe?.let { recipeDetails ->
                    val recipe = recipeDetails.toRecipe()
                    recipes.add(recipe)
                }
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

