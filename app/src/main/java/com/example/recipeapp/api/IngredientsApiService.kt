package com.example.recipeapp.api

import com.example.recipeapp.data.IngredientsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface IngredientsApiService {
    @GET("list.php?i=list")
    suspend fun getIngredients(): IngredientsResponse

}