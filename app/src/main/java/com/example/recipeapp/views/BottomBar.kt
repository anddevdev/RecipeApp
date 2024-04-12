package com.example.recipeapp.views


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.recipeapp.data.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val bottomNavigationItems = listOf(
        Screen.RecipeScreen.route to Icons.Default.Home,
        Screen.FavoriteRecipesScreen.route to Icons.Default.Favorite,
        Screen.ProfileScreen.route to Icons.Default.Person
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BottomNavigation(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.Blue,
        ) {
            bottomNavigationItems.forEach { (route, icon) ->
                BottomNavigationItem(
                    icon = {
                        Icon(
                            icon,
                            contentDescription = null
                        )
                    },
                    selected = currentRoute == route,
                    onClick = {
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(Screen.RecipeScreen.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
