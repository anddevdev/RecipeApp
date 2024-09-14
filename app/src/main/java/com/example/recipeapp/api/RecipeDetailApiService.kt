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

    companion object {
        val instance: RecipeDetailsApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeDetailsApiService::class.java)
        }
    }
}