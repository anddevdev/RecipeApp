package com.example.recipeapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String
) : Parcelable

data class RecipesResponse(val meals: List<Recipe>)