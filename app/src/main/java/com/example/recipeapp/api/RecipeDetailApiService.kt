package com.example.recipeapp.api

import com.example.recipeapp.data.RecipeDetailsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeDetailsApiService {
    @GET("search.php")
    suspend fun getRecipeByName(@Query("s") mealStr: String): RecipeDetailsResponse

    @GET("lookup.php")
    suspend fun getRecipeById(@Query("i") id: String): RecipeDetailsResponse?

}