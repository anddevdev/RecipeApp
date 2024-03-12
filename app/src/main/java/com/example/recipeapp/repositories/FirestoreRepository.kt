package com.example.recipeapp.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addFavorite(userId: String, recipeId: String, thumbnailUrl: String, category: String) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.set(mapOf(
            "recipeId" to recipeId,
            "thumbnailUrl" to thumbnailUrl,
            "category" to category
        ))
    }

    suspend fun removeFavorite(userId: String, recipeId: String) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.delete()
    }


    suspend fun getFavorites(userId: String): List<Triple<String, String, String>> {
        val favoritesSnapshot = firestore.collection("users").document(userId)
            .collection("favorites").get().await()
        val favoriteRecipes = mutableListOf<Triple<String, String, String>>()
        for (document in favoritesSnapshot.documents) {
            val recipeId = document.id
            val category = document.getString("category") ?: ""
            val thumbnailUrl = document.getString("thumbnailUrl") ?: ""
            favoriteRecipes.add(Triple(recipeId, category, thumbnailUrl))
        }
        return favoriteRecipes
    }
}