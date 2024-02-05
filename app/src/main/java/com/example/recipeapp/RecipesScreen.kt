package com.example.recipeapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.recipeapp.Recipe
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun RecipesScreen(viewModel: RecipeViewModel, category: Category) {
    val recipesState by viewModel.recipesState

    LaunchedEffect(category) {
        viewModel.fetchRecipesByCategory(category.strCategory)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                recipesState.loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                recipesState.error != null -> {
                    Text("Error Occurred")
                }
                else -> {
                    LazyColumn {
                        items(recipesState.list) { recipe ->
                            RecipeItem(recipe = recipe)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
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


