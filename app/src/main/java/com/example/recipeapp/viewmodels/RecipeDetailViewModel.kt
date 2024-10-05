package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.data.Note
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val recipeDetailsApiService: RecipeDetailsApiService
) : ViewModel() {

    private val _recipeDetailState = MutableLiveData<RecipeDetailState>()
    val recipeDetailState: LiveData<RecipeDetailState> = _recipeDetailState

    private val _existingNotes = MutableLiveData<List<Note>>()
    val existingNotes: LiveData<List<Note>> = _existingNotes

    private val _recipeRating = MutableLiveData<Pair<Double, Int>>() // averageRating, ratingCount
    val recipeRating: LiveData<Pair<Double, Int>> get() = _recipeRating


    init {
        _recipeDetailState.value = RecipeDetailState(loading = true)
    }

    fun getRecipeDetails(recipeId: String) {
        viewModelScope.launch {
            try {
                val response = recipeDetailsApiService.getRecipeById(recipeId)
                val recipe = response?.meals?.firstOrNull()
                if (recipe != null) {
                    _recipeDetailState.postValue(RecipeDetailState(recipe = recipe, loading = false))
                } else {
                    _recipeDetailState.postValue(RecipeDetailState(loading = false, error = "Recipe not found"))
                }
            } catch (e: Exception) {
                _recipeDetailState.postValue(RecipeDetailState(loading = false, error = "Error fetching recipe details"))
                Log.e("RecipeDetailViewModel", "Error fetching recipe details: ${e.message}")
            }
        }
    }

    // Add or update a note for the recipe
    suspend fun addNote(note: Note, userId: String) {
        try {
            firestoreRepository.addNote(note, userId)
            val updatedNotes = _existingNotes.value?.toMutableList() ?: mutableListOf()
            updatedNotes.add(note)
            _existingNotes.postValue(updatedNotes)
        } catch (e: Exception) {
            // Handle exception
            Log.e("RecipeDetailsViewModel", "Error adding note: ${e.message}", e)
        }
    }

    suspend fun updateNote(noteId: String, updatedContent: Note, userId: String) {
        try {
            firestoreRepository.updateNote(userId, noteId, updatedContent)
            val updatedNotes = _existingNotes.value?.toMutableList() ?: mutableListOf()
            val index = updatedNotes.indexOfFirst { it.noteId == noteId }
            if (index >= 0) {
                updatedNotes[index] = updatedContent  // Replace the note with the updated content
                _existingNotes.postValue(updatedNotes)  // Trigger LiveData update
            }
            Log.d("NOTEUPDATE", "UPDATEDNOTE: $updatedContent , NOTEID: $noteId , USERID: $userId")
        } catch (e: Exception) {
            // Handle exception
            Log.e("RecipeDetailsViewModel", "Error updating note: ${e.message}", e)
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
        val updatedNotes = _existingNotes.value?.toMutableList() ?: mutableListOf()
        updatedNotes.removeAll { it.noteId == noteId }
        _existingNotes.postValue(updatedNotes)
    }

    fun addRating(recipeId: String, userId: String, rating: Int) {
        viewModelScope.launch {
            firestoreRepository.addRecipeRating(recipeId, userId, rating)
            updateRecipeRating(recipeId)
        }
    }

    fun updateRecipeRating(recipeId: String?) {
        if (recipeId.isNullOrBlank()) {
            Log.e("RecipeDetailViewModel", "Invalid recipeId: $recipeId")
            return
        }

        viewModelScope.launch {
            val rating = firestoreRepository.getRecipeRating(recipeId)
            _recipeRating.value = rating
        }
    }

    data class RecipeDetailState(
        val recipe: RecipeDetails? = null,
        val loading: Boolean = true,
        val error: String? = null
    )
}
