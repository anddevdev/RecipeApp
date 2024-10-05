package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.api.RecipeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeApiService: RecipeApiService
) : ViewModel() {

    private val _recipesState = mutableStateOf(RecipesState())

    val recipesState: State<RecipesState> = _recipesState


    fun fetchRecipesForCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                Log.d("RecipeViewModel", "Fetching recipes for category: $categoryName")
                _recipesState.value = RecipesState(loading = true)
                val response = recipeApiService.getRecipesByCategory(categoryName)
                _recipesState.value = RecipesState(list = response.meals, loading = false)
                Log.d("RecipeViewModel", "Recipes fetched successfully.")
            } catch (e: Exception) {
                _recipesState.value = RecipesState(error = e.localizedMessage, loading = false)
            }
        }
    }

    data class RecipesState(
        val loading: Boolean = true,
        val list: List<Recipe> = emptyList(),
        val error: String? = null
    )
}