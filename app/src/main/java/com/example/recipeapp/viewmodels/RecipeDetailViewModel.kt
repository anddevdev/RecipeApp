package com.example.recipeapp.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.data.Note
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.repositories.FirestoreRepository
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val firestoreRepository: FirestoreRepository) : ViewModel() {

    private val _recipeDetailState = MutableLiveData<RecipeDetailState>()
    val recipeDetailState: LiveData<RecipeDetailState> = _recipeDetailState

    private val _existingNotes = MutableLiveData<List<Note>>()
    val existingNotes: LiveData<List<Note>> = _existingNotes

    init {
        _recipeDetailState.value = RecipeDetailState(loading = true)
    }

    fun fetchRecipeDetailsIfNeeded(recipeName: String, userId: String) {
        if (_recipeDetailState.value?.recipe == null) {
            fetchRecipeDetails(recipeName, userId)
        }
    }

    private fun fetchRecipeDetails(recipeName: String, userId: String) {
        viewModelScope.launch {
            try {
                Log.d("RecipeDetailsViewModel", "Fetching details for recipe: $recipeName")
                val response = RecipeDetailsApiService.recipeDetailApiService.getRecipeByName(recipeName)
                Log.d("RecipeDetailsViewModel", "Response: $response")
                val recipe = response.meals.firstOrNull()
                if (recipe != null) {
                    _recipeDetailState.value = RecipeDetailState(recipe = recipe, loading = false, error = null)
                    getNotesForRecipeAndUser(recipe.idMeal, userId) // Pass userId here
                } else {
                    _recipeDetailState.value = RecipeDetailState(loading = false, error = "Recipe not found")
                }
            } catch (e: Exception) {
                _recipeDetailState.value = RecipeDetailState(loading = false, error = "Error fetching recipe details")
            }
        }
    }

    // Add or update a note for the recipe
    suspend fun addNote(note: Note, userId: String) {
        try {
            firestoreRepository.addNote(note, userId)
        } catch (e: Exception) {
            // Handle exception
            Log.e(TAG, "Error adding note: ${e.message}", e)
        }
    }

    suspend fun updateNote(noteId: String, updatedContent: Note, userId: String) {
        try {
            firestoreRepository.updateNote(userId, noteId, updatedContent)
            Log.d("NOTEUPDATE", "UPDATEDNOTE: $updatedContent , NOTEID: $noteId , USERID: $userId")
        } catch (e: Exception) {
            // Handle exception
            Log.e(TAG, "Error updating note: ${e.message}", e)
        }
    }

    // Fetch existing notes for the recipe and the current user
    fun getNotesForRecipeAndUser(recipeId: String, userId: String) {
        viewModelScope.launch {
            _existingNotes.value = firestoreRepository.getNotesForRecipeAndUser(recipeId, userId)
        }
    }

    // Delete a note by ID
    suspend fun deleteNoteById(noteId: String, userId: String) {
        firestoreRepository.deleteNoteById(noteId, userId)
    }

    data class RecipeDetailState(
        val recipe: RecipeDetails? = null,
        val loading: Boolean = true,
        val error: String? = null
    )
}
