package com.example.recipeapp

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.data.Category
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.Screen
import com.example.recipeapp.viewmodels.FavoritesViewModel
import com.example.recipeapp.viewmodels.IngredientsViewModel
import com.example.recipeapp.viewmodels.LoginViewModel
import com.example.recipeapp.viewmodels.ProfileViewModel
import com.example.recipeapp.viewmodels.RecipeDetailViewModel
import com.example.recipeapp.viewmodels.RecipeViewModel
import com.example.recipeapp.viewmodels.RegistrationViewModel
import com.example.recipeapp.views.*
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecipeApp(
    navController: NavHostController = rememberNavController(),
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val registrationViewModel: RegistrationViewModel = hiltViewModel()
    val isLoggedOut by loginViewModel.isLoggedOut.observeAsState(initial = true)
    val isRegistered by registrationViewModel.isRegistered.observeAsState(initial = false)
    val isLoggedInAnonymously by loginViewModel.isLoggedInAnonymously.observeAsState(initial = false)


    var showLogoutDialog by remember { mutableStateOf(false) }

    Log.d("RecipeApp", "isLoggedOut: $isLoggedOut, isLoggedInAnonymously: $isLoggedInAnonymously, isRegistered: $isRegistered")

    Scaffold(
        bottomBar = {
            if (!isLoggedOut || isRegistered || isLoggedInAnonymously) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) {
        // Apply padding to the content area to prevent overlap with the bottom bar
        Box(modifier = Modifier.padding(bottom = if (!isLoggedOut || isLoggedInAnonymously || isRegistered) 56.dp else 0.dp)) {
            NavHost(
                navController = navController,
                startDestination = determineStartDestination(isLoggedOut, isLoggedInAnonymously)
            ) {
                composable(route = Screen.RecipeScreen.route) {
                    Log.d("RecipeApp", "Showing RecipeScreen")
                    RecipeScreen(
                        navigateToDetail = { category ->
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "cat",
                                category
                            )
                            navController.navigate(Screen.DetailScreen.route)
                        }
                    )
                }

                composable(route = Screen.DetailScreen.route) {
                    val category =
                        navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                            ?: Category("", "", "", "")
                    CategoryDetailScreen(category = category) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("cat", category)
                        navController.navigate(Screen.RecipesScreen.route)
                    }
                }

                composable(route = Screen.RecipesScreen.route) {
                    Log.d("RecipeApp", "Showing RecipesScreen")
                    val recipesViewModel: RecipeViewModel = hiltViewModel()
                    val recipesState by recipesViewModel.recipesState
                    val category =
                        navController.previousBackStackEntry?.savedStateHandle?.get<Category>("cat")
                            ?: Category("", "", "", "")
                    RecipesScreen(recipesViewModel, category = category) { recipe ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("recipe", recipe)
                        navController.navigate(Screen.RecipeDetailsScreen.route)
                    }
                }

                composable(route = Screen.RecipeDetailsScreen.route) {
                    Log.d("RecipeApp", "Showing RecipeDetailsScreen")

                    val recipeDetailsViewModel: RecipeDetailViewModel = hiltViewModel()
                    val profileViewModel: ProfileViewModel = hiltViewModel()
                    val favoritesViewModel: FavoritesViewModel = hiltViewModel()

                    // Retrieve the recipe from the previous back stack entry
                    val recipe = navController.previousBackStackEntry?.savedStateHandle?.get<Recipe>("recipe")
                        ?: Recipe("", "", "", "", "")

                    // Match the signature of the RecipeDetailScreen function
                    RecipeDetailScreen(
                        recipe = recipe,
                        viewModel = recipeDetailsViewModel,
                        favoritesViewModel = favoritesViewModel,
                        profileViewModel = profileViewModel,
                        navigateToFavoriteRecipes = {
                            navController.navigate(Screen.FavoriteRecipesScreen.route) {
                                popUpTo(Screen.RecipeScreen.route) { inclusive = false }
                            }
                        }
                    )
                }


                composable(route = Screen.LoginScreen.route) {
                    Log.d("RecipeApp", "Showing LoginScreen")
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.RecipeScreen.route) {
                                popUpTo(Screen.RecipeScreen.route) { inclusive = true }
                            }
                        },
                        onRegisterClick = { navController.navigate(Screen.RegisterScreen.route) },
                        viewModel = loginViewModel
                    )
                }

                composable(route = Screen.RegisterScreen.route) {
                    Log.d("RecipeApp", "Showing RegisterScreen")
                    RegisterScreen(
                        onRegistrationSuccess = {
                            navController.navigate(Screen.RecipeScreen.route) {
                                popUpTo(Screen.RecipeScreen.route) { inclusive = true }
                            }
                        },
                        viewModel = registrationViewModel
                    )
                }

                composable(Screen.FavoriteRecipesScreen.route) {
                    Log.d("RecipeApp", "Showing FavoriteRecipesScreen")
                    FavoriteRecipesScreen()
                }

                composable(route = Screen.ProfileScreen.route) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val profileViewModel: ProfileViewModel = hiltViewModel()
                        val ingredientsViewModel: IngredientsViewModel = hiltViewModel()
                        ProfileScreen(userId, profileViewModel, ingredientsViewModel)
                    } else {
                        Log.e("RecipeApp", "User ID is null")
                    }
                }

                composable(route = Screen.RecommendationsScreen.route) {
                    RecommendationsScreen(
                        onRecipeClick = { recipe ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("recipe", recipe)
                            navController.navigate(Screen.RecipeDetailsScreen.route)
                        }
                    )
                }
            }


            if (showLogoutDialog) {
                Log.d("RecipeApp", "showLogoutDialog is true. Showing Logout Dialog")
                LogoutDialog(
                    onLogout = {
                        loginViewModel.logout(registrationViewModel)
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                        showLogoutDialog = false
                    },
                    onCancel = {
                        showLogoutDialog = false
                    }
                )
            } else {
                Log.d("RecipeApp", "showLogoutDialog is false. Not showing Logout Dialog")
            }

            BackHandler {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                Log.d("BackHandler", "Current route: $currentRoute, isLoggedOut: $isLoggedOut, isLoggedInAnonymously: $isLoggedInAnonymously, isRegistered: $isRegistered")

                if (!isLoggedOut || isLoggedInAnonymously || isRegistered) {
                    // User is logged in
                    if (currentRoute == Screen.RecipeScreen.route) {
                        Log.d("BackHandler", "Show logout dialog")
                        // Show the logout dialog if the user is on the recipe screen
                        showLogoutDialog = true
                    } else if (currentRoute == Screen.FavoriteRecipesScreen.route) {
                        Log.d("BackHandler", "Navigate to RecipeScreen")
                        navController.navigate(Screen.RecipeScreen.route)
                    } else if (currentRoute == Screen.ProfileScreen.route) {
                        Log.d("BackHandler", "Navigate to RecipeScreen")
                        navController.navigate(Screen.RecipeScreen.route)
                    } else {
                        Log.d("BackHandler", "Pop back stack: User not on recipe or favorites screen")
                        // Handle back navigation for other screens
                        navController.popBackStack()
                    }
                } else {
                    if (currentRoute == Screen.RecipeScreen.route && isLoggedInAnonymously) {
                        Log.d("BackHandler", "Show logout dialog for anonymous user")
                        // Show the logout dialog if the user is logged in anonymously and on the recipe screen
                        showLogoutDialog = true
                    } else {
                        Log.d("BackHandler", "Pop back stack: User logged out or not on recipe screen")
                        // Handle back navigation for other cases
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

private fun determineStartDestination(isLoggedOut: Boolean, isLoggedInAnonymously: Boolean): String {
    return if (isLoggedOut || !isLoggedInAnonymously) {
        Log.d("RecipeApp", "Start Destination: LoginScreen")
        Screen.LoginScreen.route
    } else {
        Log.d("RecipeApp", "Start Destination: RecipeScreen")
        Screen.RecipeScreen.route
    }
}

