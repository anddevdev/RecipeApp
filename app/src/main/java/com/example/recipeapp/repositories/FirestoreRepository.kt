package com.example.recipeapp.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.example.recipeapp.data.Note
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()

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

    suspend fun addOrUpdateNote(note: Note, userId: String) {
        firestore.collection("users").document(userId)
            .collection("notes").document(note.noteId)
            .set(note)
            .addOnSuccessListener {
                Log.d(TAG, "Note added/updated successfully with ID: ${note.noteId}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding/updating note with ID: ${note.noteId}", e)
            }
            .await()
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
}




