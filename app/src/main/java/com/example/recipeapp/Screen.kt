package com.example.recipeapp

sealed class Screen(val route : String) {

    object RecipeScreen : Screen (route = "RecipeString")
    object DetailScreen : Screen (route = "DetailString")

}