package com.example.recipeapp.data

sealed class Screen(val route : String) {

    object RecipeScreen : Screen(route = "RecipeString")
    object DetailScreen : Screen(route = "DetailString")
    object RecipesScreen : Screen(route = "RecipesScreen")
    object RecipeDetailsScreen : Screen(route = "RecipeDetailsScreen")
    object LoginScreen : Screen(route = "LoginScreen")
    object RegisterScreen : Screen(route = "RegisterScreen")
    object FavoriteRecipesScreen : Screen(route = "FavoriteRecipesScreen")

}