package com.example.recipeapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.recipeapp.repositories.FirestoreRepository

class FavoritesViewModel(private val firestoreRepository: FirestoreRepository) : ViewModel() {
    
    suspend fun addFavorite(userId: String, recipeId: String, thumbnailUrl: String) {
        firestoreRepository.addFavorite(userId, recipeId, thumbnailUrl)
    }

    suspend fun removeFavorite(userId: String, recipeId: String) {
        firestoreRepository.removeFavorite(userId, recipeId)

    }

    suspend fun getFavorites(userId: String): List<Pair<String, String>> {
        return firestoreRepository.getFavorites(userId)
    }
}