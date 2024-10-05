package com.example.recipeapp.data

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route : String) {

    object RecipeScreen : Screen(route = "RecipeString")
    object DetailScreen : Screen(route = "DetailString")
    object RecipesScreen : Screen(route = "RecipesScreen/{categoryName}") {
        fun createRoute(categoryName: String) = "RecipesScreen/$categoryName"
        val arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
    }
    object RecipeDetailsScreen : Screen(route = "RecipeDetailsScreen/{recipeId}") {
        fun createRoute(recipeId: String) = "RecipeDetailsScreen/$recipeId"
        val arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
    }
    object LoginScreen : Screen(route = "LoginScreen")
    object RegisterScreen : Screen(route = "RegisterScreen")
    object FavoriteRecipesScreen : Screen(route = "FavoriteRecipesScreen")
    object ProfileScreen : Screen("ProfileScreen")
    object RecommendationsScreen : Screen("RecommendationsScreen")

}