package com.example.recipeapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recipeapp.data.Category
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.Screen
import com.example.recipeapp.viewmodels.LoginViewModel
import com.example.recipeapp.viewmodels.MainViewModel
import com.example.recipeapp.viewmodels.RecipeDetailViewModel
import com.example.recipeapp.viewmodels.RecipeViewModel
import com.example.recipeapp.viewmodels.RegistrationViewModel
import com.example.recipeapp.views.CategoryDetailScreen
import com.example.recipeapp.views.LoginScreen
import com.example.recipeapp.views.RecipeDetailScreen
import com.example.recipeapp.views.RecipeScreen
import com.example.recipeapp.views.RecipesScreen
import com.example.recipeapp.views.RegisterScreen

@Composable
fun RecipeApp(navController: NavHostController,
              isLoggedIn: Boolean,
              isLoggedOut: Boolean,
              isRegistered: Boolean,
              loginViewModel: LoginViewModel,
              registrationViewModel: RegistrationViewModel
) {
    val recipeViewModel: MainViewModel = viewModel()
    val viewstate by recipeViewModel.categoriesState

    NavHost(navController = navController, startDestination = determineStartDestination(isLoggedOut, isRegistered)) {
        // Recipe screen
        composable(route = Screen.RecipeScreen.route) {
            RecipeScreen(
                viewstate = viewstate,
                navigateToDetail = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("cat", it)
                    navController.navigate(Screen.DetailScreen.route)
                }
            )
        }

        // Detail screen
        composable(route = Screen.DetailScreen.route) {
            val category =
                navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                    ?: Category("", "", "", "")
            CategoryDetailScreen(category = category) {
                navController.currentBackStackEntry?.savedStateHandle?.set("cat", category)
                navController.navigate(Screen.RecipesScreen.route)
            }
        }

        // Recipes screen
        composable(route = Screen.RecipesScreen.route) {
            val recipesViewModel: RecipeViewModel = viewModel()
            val recipesState by recipesViewModel.recipesState

            val category =
                navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                    ?: Category("", "", "", "")

            RecipesScreen(recipesViewModel, category = category) { recipe ->
                navController.currentBackStackEntry?.savedStateHandle?.set("recipe", recipe)
                navController.navigate(Screen.RecipeDetailsScreen.route)
            }
        }

        // Recipe details screen
        composable(route = Screen.RecipeDetailsScreen.route) {
            val recipeDetailsViewModel: RecipeDetailViewModel = viewModel()
            val recipeDetailState by recipeDetailsViewModel.recipeDetailState

            val recipe = navController.previousBackStackEntry?.savedStateHandle?.get<Recipe>("recipe")
                ?: Recipe("", "", "", "")

            RecipeDetailScreen(recipeDetailsViewModel, recipe = recipe)
        }

        // Login screen
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.RecipeScreen.route) {
                        // Pop up to the recipe screen and remove the login screen from back stack
                        popUpTo(Screen.RecipeScreen.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.RegisterScreen.route)
                },
                viewModel = loginViewModel
            )
        }

        // Register screen
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(
                onRegistrationSuccess = {
                    navController.navigate(Screen.RecipeScreen.route) {
                        // Pop up to the recipe screen and remove the register screen from back stack
                        popUpTo(Screen.RecipeScreen.route) { inclusive = true }
                    }
                },
                viewModel = registrationViewModel
            )
        }
    }
}


private fun determineStartDestination(isLoggedOut: Boolean, isRegistered: Boolean): String {
    return if (isLoggedOut) {
        Screen.LoginScreen.route
    } else if (isRegistered) {
        Screen.RecipeScreen.route
    } else {
        Screen.RegisterScreen.route
    }
}
