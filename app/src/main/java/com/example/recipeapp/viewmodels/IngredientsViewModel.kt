package com.example.recipeapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.api.IngredientsApiService
import com.example.recipeapp.data.Ingredient
import kotlinx.coroutines.launch

class IngredientsViewModel(private val apiService: IngredientsApiService) : ViewModel() {

    private val _ingredients = MutableLiveData<List<Ingredient>?>()
    val ingredients: MutableLiveData<List<Ingredient>?> = _ingredients

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchIngredients() {
        viewModelScope.launch {
            try {
                val response = apiService.getIngredients()
                _ingredients.value = response.ingredients
            } catch (e: Exception) {
                _error.value = "Failed to fetch ingredients: ${e.message}"
            }
        }
    }

    fun clearIngredients() {
        _ingredients.value = null
    }

}