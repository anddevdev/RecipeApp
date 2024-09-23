package com.example.recipeapp.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.data.Category
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.viewmodels.RecipeViewModel

@Composable
fun RecipesScreen(viewModel: RecipeViewModel = hiltViewModel(),
                  category: Category,
                  onRecipeClick: (Recipe) -> Unit) {

    val recipesState by viewModel.recipesState
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(category) {
        viewModel.fetchRecipesByCategory(category.strCategory)
    }

    // Filter recipes based on search query
    val filteredRecipes = recipesState.list.filter {
        it.strMeal.contains(searchQuery, ignoreCase = true)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search recipes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    recipesState.loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    recipesState.error != null -> {
                        Text("Error Occurred", modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        LazyColumn {
                            items(filteredRecipes) { recipe ->
                                RecipeItem(recipe = recipe, onRecipeClick = onRecipeClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe , onRecipeClick: (Recipe) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium)
            .clickable { onRecipeClick(recipe) } // Handle recipe item click
    ) {
        Image(
            painter = rememberAsyncImagePainter(recipe.strMealThumb),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
    }

    Row {
        // Display the recipe name in the middle
        Text(
            text = recipe.strMeal,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp)
                .align(Alignment.Top),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
    }
}
