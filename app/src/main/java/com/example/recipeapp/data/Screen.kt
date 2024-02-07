package com.example.recipeapp.data

sealed class Screen(val route : String) {

    object RecipeScreen : Screen(route = "RecipeString")
    object DetailScreen : Screen(route = "DetailString")
    object RecipesScreen : Screen(route = "RecipesScreen")

}