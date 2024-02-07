package com.example.recipeapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recipeapp.data.Category
import com.example.recipeapp.data.Screen
import com.example.recipeapp.viewmodels.MainViewModel
import com.example.recipeapp.viewmodels.RecipeViewModel
import com.example.recipeapp.views.CategoryDetailScreen
import com.example.recipeapp.views.RecipeScreen
import com.example.recipeapp.views.RecipesScreen

@Composable
fun RecipeApp(navController: NavHostController) {
    val recipeViewModel: MainViewModel = viewModel()
    val viewstate by recipeViewModel.categoriesState

    NavHost(navController = navController, startDestination = Screen.RecipeScreen.route) {
        composable(route = Screen.RecipeScreen.route) {
            RecipeScreen(
                viewstate = viewstate,
                navigateToDetail = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("cat", it)
                    navController.navigate(Screen.DetailScreen.route)
                })
        }

        composable(route = Screen.DetailScreen.route) {
            val category = navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                ?: Category("", "", "", "")
            CategoryDetailScreen(category = category) {
                navController.currentBackStackEntry?.savedStateHandle?.set("cat", category)
                navController.navigate(Screen.RecipesScreen.route)
            }
        }

        composable(route = Screen.RecipesScreen.route) {
            val recipesViewModel: RecipeViewModel = viewModel()
            val recipesState by recipesViewModel.recipesState

            val category = navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                ?: Category("", "", "", "")

            RecipesScreen(recipesViewModel, category = category)
        }
    }
}
