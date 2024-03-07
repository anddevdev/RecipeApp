package com.example.recipeapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.recipeapp.repositories.FirestoreRepository

class FavoritesViewModel(private val firestoreRepository: FirestoreRepository) : ViewModel() {

    // Function to add a recipe to favorites
    suspend fun addFavorite(userId: String, recipeId: String) {
        firestoreRepository.addFavorite(userId, recipeId)
    }

    // Function to remove a recipe from favorites
    suspend fun removeFavorite(userId: String, recipeId: String) {
        firestoreRepository.removeFavorite(userId, recipeId)
    }

    // Function to get a user's favorite recipes
    suspend fun getFavorites(userId: String): List<String> {
        return firestoreRepository.getFavorites(userId)
    }
}