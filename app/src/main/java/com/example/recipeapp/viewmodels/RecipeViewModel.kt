package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.api.RecipeApiService
import kotlinx.coroutines.launch


class RecipeViewModel : ViewModel() {

    private val _recipesState = mutableStateOf(RecipesState())

    val recipesState: State<RecipesState> = _recipesState



fun fetchRecipesByCategory(category: String) {
    viewModelScope.launch {
        try {
            Log.d("RecipeViewModel", "Fetching recipes for category: $category")
            val response = RecipeApiService.recipeApiService.getRecipesByCategory(category)
            _recipesState.value = _recipesState.value.copy(
                list = response.meals ?: emptyList(),
                loading = false,
                error = null
            )
            Log.d("RecipeViewModel", "Recipes fetched successfully.")
        } catch (e: Exception) {
            _recipesState.value = _recipesState.value.copy(
                loading = false,
                error = "Error fetching recipes: ${e.message}"
            )
            Log.e("RecipeViewModel", "Error fetching recipes: ${e.message}")
        }
    }
}

    data class RecipesState(
        val loading: Boolean = true,
        val list: List<Recipe> = emptyList(),
        val error: String? = null
    )
}