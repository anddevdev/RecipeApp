package com.example.recipeapp.data

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("idIngredient") val id: String,
    @SerializedName("strIngredient") val name: String,
    @SerializedName("strDescription") val description: String? // Description might be nullable
)

data class IngredientsResponse(
    @SerializedName("meals") val ingredients: List<Ingredient>
)