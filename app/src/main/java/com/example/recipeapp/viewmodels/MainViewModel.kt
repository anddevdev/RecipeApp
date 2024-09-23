package com.example.recipeapp.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.Category
import com.example.recipeapp.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _categoriesState = mutableStateOf(RecipeState())

    val categoriesState  : State <RecipeState> = _categoriesState

    init {
        fetchCategories()
    }

    private fun fetchCategories(){

        viewModelScope.launch {
            try {
                val response = apiService.getCategories()
                _categoriesState.value = _categoriesState.value.copy(
                    list = response.categories,
                    loading = false,
                    error = null
                )
            }catch (e : Exception){
                categoriesState.value.copy(
                    loading = false,
                    error = "Error fetching the categories and ${e.message}"
                )

            }
        }
    }

    data class RecipeState(
        val loading : Boolean = true,
        val list: List<Category> = emptyList(),
        val error : String? = null

    )


}