package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.data.RecipeDetails
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {

    private val _recipeDetailState = mutableStateOf(RecipeDetailState())

    val recipeDetailState: State<RecipeDetailState> = _recipeDetailState

    fun fetchRecipeDetails(recipeName: String) {
        viewModelScope.launch {
            try {
                Log.d("RecipeDetailsViewModel", "Fetching details for recipe: $recipeName")
                val response = RecipeDetailsApiService.recipeDetailApiService.getRecipeByName(recipeName)
                Log.d("RecipeDetailsViewModel", "Response: $response")
                val recipe = response.meals.firstOrNull()
                if (recipe != null) {
                    _recipeDetailState.value = RecipeDetailState(recipe = recipe, loading = false, error = null)
                } else {
                    _recipeDetailState.value = RecipeDetailState(loading = false, error = "Recipe not found")
                }
            } catch (e: Exception) {
                _recipeDetailState.value = RecipeDetailState(loading = false, error = "Error fetching recipe details")
            }
        }
    }

    data class RecipeDetailState(
        val recipe: RecipeDetails? = null,
        val loading: Boolean = true,
        val error: String? = null
    )
}
