package com.example.recipeapp.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.viewmodels.RecommendationsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecommendationsScreen(
    onRecipeClick: (Recipe) -> Unit
) {
    val viewModel: RecommendationsViewModel = viewModel()
    val recommendedRecipes by viewModel.recommendedRecipes.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val showingDefaultRecommendations by viewModel.showingDefaultRecommendations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchRecommendedRecipes()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                // Show loading indicator with a message
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator() // Loading spinner
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Analyzing your preferences...")
                }
            }

            recommendedRecipes.isEmpty() -> {
                // Show message when there are no recommendations
                Text(
                    text = "No recommendations available. Please add more recipes to your favorites list so we can analyze your preferences.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                // Show the list of recommended recipes
                Column {
                    if (showingDefaultRecommendations) {
                        // Display message for default recommendations
                        Text(
                            text = "We couldn't find enough data to personalize your recommendations. Here are some popular recipes you might like!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Optional: Header for personalized recommendations
                        Text(
                            text = "Recommended for you",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.h6
                        )
                    }

                    LazyColumn {
                        items(recommendedRecipes) { recipe ->
                            RecipeItem(recipe = recipe, onClick = { onRecipeClick(recipe) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(recipe.strMealThumb),
            contentDescription = recipe.strMeal,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = recipe.strMeal,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
        )
    }
}
