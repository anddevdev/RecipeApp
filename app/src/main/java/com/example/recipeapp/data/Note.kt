package com.example.recipeapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val noteId: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val content: String = ""
) : Parcelable {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", "")
}