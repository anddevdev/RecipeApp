package com.example.recipeapp.data

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserProfile(
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("profilePictureUrl")
    @set:PropertyName("profilePictureUrl")
    var profilePictureUrl: String? = null,

    @get:PropertyName("allergies")
    @set:PropertyName("allergies")
    var allergies: List<String> = listOf()
) : Parcelable

