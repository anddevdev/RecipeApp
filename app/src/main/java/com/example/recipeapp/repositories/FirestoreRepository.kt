package com.example.recipeapp.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.data.Note
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.UserProfile
import com.example.recipeapp.data.toRecipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreRepository @Inject constructor(
    private val recipeDetailsApiService: RecipeDetailsApiService,
    private val firestore: FirebaseFirestore
) {

    // Add favorite recipe for a user
    suspend fun addFavorite(
        userId: String,
        recipeId: String,
        thumbnailUrl: String,
        category: String
    ) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.set(
            mapOf(
                "recipeId" to recipeId,
                "thumbnailUrl" to thumbnailUrl,
                "category" to category
            )
        )
    }

    // Remove favorite recipe for a user
    suspend fun removeFavorite(userId: String, recipeId: String) {
        val favoriteRef = firestore.collection("users").document(userId)
            .collection("favorites").document(recipeId)
        favoriteRef.delete()
    }

    // Get favorite recipes for a user
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

    // Get profile data for a user
    suspend fun getUserProfile(userId: String): UserProfile? {
        val profileSnapshot = firestore.collection("users").document(userId)
            .collection("profiledata").document("profile")
            .get().await()
        return profileSnapshot.toObject(UserProfile::class.java)
    }


    // Add/update user allergies
    suspend fun addUserAllergies(userId: String, allergies: List<String>) {
        val profileRef = firestore.collection("users").document(userId)
            .collection("profiledata").document("profile")
        profileRef.update("allergies", allergies)
            .addOnSuccessListener {
                Log.d(TAG, "Allergies added/updated successfully for user ID: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding/updating allergies for user ID: $userId", e)
            }
            .await()
    }

    suspend fun updateUserName(userId: String, name: String) {
        val profileRef = firestore.collection("users").document(userId)
            .collection("profiledata").document("profile")
        profileRef.update("name", name)
            .addOnSuccessListener {
                Log.d(TAG, "Username updated successfully for user ID: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating username for user ID: $userId", e)
            }
            .await()
    }

    suspend fun addProfileData(userId: String, profile: UserProfile) {
        try {
            val profileRef = firestore.collection("users").document(userId)
                .collection("profiledata").document("profile")
            profileRef.set(profile)
                .addOnSuccessListener {
                    // Log success or perform any additional actions
                    Log.d(TAG, "Profile data added successfully for user ID: $userId")
                }
                .addOnFailureListener { e ->
                    // Log error or handle failure
                    Log.e(TAG, "Error adding profile data for user ID: $userId", e)
                }
                .await() // Wait for the operation to complete
        } catch (e: Exception) {
            // Handle exceptions
            Log.e(TAG, "Error adding profile data for user ID: $userId", e)
            throw e // Rethrow the exception for further handling if needed
        }
    }

    suspend fun addNote(note: Note, userId: String) {
        val noteRef = firestore.collection("users").document(userId)
            .collection("notes").document(note.noteId)
        noteRef.set(note)
            .addOnSuccessListener {
                Log.d(TAG, "Note added successfully with ID: ${note.noteId}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding note with ID: ${note.noteId}", e)
            }
            .await()
    }

    suspend fun updateNote(userId: String, noteId: String, updatedContent: Note) {
        try {
            val noteRef = firestore.collection("users").document(userId)
                .collection("notes").document(noteId)
            noteRef.set(updatedContent, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Note updated successfully with ID: $noteId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating note with ID: $noteId", e)
                }
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}", e)
        }
    }

    suspend fun getNotesForRecipeAndUser(recipeId: String, userId: String): List<Note> {
        val notesSnapshot = firestore.collection("users").document(userId)
            .collection("notes")
            .whereEqualTo("recipeId", recipeId)
            .get()
            .await()
        return notesSnapshot.toObjects(Note::class.java)
    }

    suspend fun deleteNoteById(noteId: String, userId: String) {
        val noteRef = firestore.collection("users").document(userId)
            .collection("notes").document(noteId)
        noteRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Note deleted successfully: $noteId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting note: $noteId", e)
            }
            .await()
    }

    suspend fun getFavoriteRecipesWithDetails(userId: String): List<Recipe> {
        val favoriteRecipes = getFavorites(userId)
        val recipes = mutableListOf<Recipe>()

        for ((recipeName, _, _) in favoriteRecipes) {
            val recipeDetailsResponse = recipeDetailsApiService.getRecipeByName(recipeName)
            val matchingRecipes = recipeDetailsResponse.meals.filter { it.strMeal == recipeName }
            val recipeDetails = matchingRecipes.firstOrNull()
            recipeDetails?.let {
                val recipe = it.toRecipe() // Convert RecipeDetails to Recipe
                recipes.add(recipe)
            }
        }
        return recipes
    }

    suspend fun addRecipeRating(recipeId: String, userId: String, rating: Int) {
        val recipeRef = firestore.collection("recipes").document(recipeId)
        val ratingRef = recipeRef.collection("ratings").document(userId)

        // Add or update the rating for the user
        ratingRef.set(mapOf("rating" to rating))
            .addOnSuccessListener {
                Log.d("FirestoreRepository", "Rating added/updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error adding/updating rating: ${e.message}")
            }

        // Check if the recipe document exists before updating it
        val recipeSnapshot = recipeRef.get().await()

        if (!recipeSnapshot.exists()) {
            // Create the document if it doesn't exist and initialize averageRating and ratingCount
            recipeRef.set(mapOf(
                "averageRating" to rating.toDouble(),  // Initial rating is the user's rating
                "ratingCount" to 1                     // First rating added
            ))
        } else {
            // Recalculate average rating after a new rating is added
            val ratingsSnapshot = recipeRef.collection("ratings").get().await()
            val totalRatings = ratingsSnapshot.documents.size
            val totalRatingValue = ratingsSnapshot.documents.sumOf { it.getLong("rating") ?: 0L }

            val newAverageRating = if (totalRatings > 0) {
                totalRatingValue.toDouble() / totalRatings
            } else {
                0.0
            }

            // Update average rating and rating count in the recipe document
            recipeRef.update(
                mapOf(
                    "averageRating" to newAverageRating,
                    "ratingCount" to totalRatings
                )
            ).addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error updating average rating: ${e.message}")
            }
        }
    }

    suspend fun getRecipeRating(recipeId: String?): Pair<Double, Int> {
        if (recipeId.isNullOrBlank()) {
            Log.e("FirestoreRepository", "Invalid recipeId: $recipeId")
            return Pair(0.0, 0)
        }

        val recipeRef = firestore.collection("recipes").document(recipeId)
        val recipeSnapshot = recipeRef.get().await()

        val averageRating = recipeSnapshot.getDouble("averageRating") ?: 0.0
        val ratingCount = recipeSnapshot.getLong("ratingCount")?.toInt() ?: 0
        return Pair(averageRating, ratingCount)
    }
}




