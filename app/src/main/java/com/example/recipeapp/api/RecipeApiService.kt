package com.example.recipeapp.api

import com.example.recipeapp.data.RecipesResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApiService {
    @GET("filter.php")
    suspend fun getRecipesByCategory(@Query("c") category: String): RecipesResponse

    companion object {
        private val retrofit = Retrofit.Builder().baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val recipeApiService: RecipeApiService = retrofit.create(RecipeApiService::class.java)
    }
}
