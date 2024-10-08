package com.example.recipeapp.viewmodels

import androidx.lifecycle.ViewModel
import com.example.recipeapp.repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    suspend fun addFavorite(userId: String, recipeId: String, thumbnailUrl: String, category: String) {
        firestoreRepository.addFavorite(userId, recipeId, thumbnailUrl, category)
    }

    suspend fun removeFavorite(userId: String, recipeId: String) {
        firestoreRepository.removeFavorite(userId, recipeId)

    }

    suspend fun getFavorites(userId: String): List<Triple<String, String, String>> {
        return firestoreRepository.getFavorites(userId)
    }
}