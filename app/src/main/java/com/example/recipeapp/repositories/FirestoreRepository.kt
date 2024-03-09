package com.example.recipeapp.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addFavorite(userId: String, recipeId: String, thumbnailUrl: String) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.set(mapOf("recipeId" to recipeId, "thumbnailUrl" to thumbnailUrl))
    }

    suspend fun removeFavorite(userId: String, recipeId: String) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.delete()
    }


    suspend fun getFavorites(userId: String): List<Pair<String, String>> {
        val favoritesSnapshot = firestore.collection("users").document(userId)
            .collection("favorites").get().await()
        val favoriteRecipes = mutableListOf<Pair<String, String>>()
        for (document in favoritesSnapshot.documents) {
            val recipeId = document.id
            val thumbnailUrl = document.getString("thumbnailUrl") ?: ""
            favoriteRecipes.add(Pair(recipeId, thumbnailUrl))
        }
        return favoriteRecipes
    }
}