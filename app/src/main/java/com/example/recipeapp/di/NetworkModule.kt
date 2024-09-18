package com.example.recipeapp.di

import com.example.recipeapp.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"


    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }


    @Singleton
    @Provides
    fun provideIngredientsApiService(retrofit: Retrofit): IngredientsApiService {
        return retrofit.create(IngredientsApiService::class.java)
    }


    @Singleton
    @Provides
    fun provideRecipeApiService(retrofit: Retrofit): RecipeApiService {
        return retrofit.create(RecipeApiService::class.java)
    }


    @Singleton
    @Provides
    fun provideRecipeDetailsApiService(retrofit: Retrofit): RecipeDetailsApiService {
        return retrofit.create(RecipeDetailsApiService::class.java)
    }
}