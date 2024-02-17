package com.example.recipeapp.views


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.viewmodels.RecipeDetailViewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.recipeapp.data.Recipe


@Composable
fun RecipeDetailScreen(viewModel: RecipeDetailViewModel, recipe: Recipe) {

    val recipeDetailState = viewModel.recipeDetailState.value

    LaunchedEffect(recipe) {
        viewModel.fetchRecipeDetails(recipe.strMeal)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            if (recipeDetailState.loading) {
                // Show loading indicator
                CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (recipeDetailState.error != null) {
                // Show error message
                Text(
                    text = "Error: ${recipeDetailState.error}",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            } else {
                // Show recipe details
                val recipe = recipeDetailState.recipe
                if (recipe != null) {
                    Column {
                        Text(
                            text = recipe.strMeal,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Image(
                            painter = rememberAsyncImagePainter(recipe.strMealThumb),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(shape = MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )

                        // Display recipe instructions
                        Text(
                            text = "Instructions:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = recipe.strInstructions,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Display ingredients and measures
                        Text(
                            text = "Ingredients:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        for (i in 1..20) { // Assuming maximum 20 ingredients
                            val ingredient = when (i) {
                                1 -> recipe.strIngredient1
                                2 -> recipe.strIngredient2
                                3 -> recipe.strIngredient3
                                4 -> recipe.strIngredient4
                                5 -> recipe.strIngredient5
                                6 -> recipe.strIngredient6
                                7 -> recipe.strIngredient7
                                8 -> recipe.strIngredient8
                                9 -> recipe.strIngredient9
                                10 -> recipe.strIngredient10
                                11 -> recipe.strIngredient11
                                12 -> recipe.strIngredient12
                                13 -> recipe.strIngredient13
                                14 -> recipe.strIngredient14
                                15 -> recipe.strIngredient15
                                16 -> recipe.strIngredient16
                                17 -> recipe.strIngredient17
                                18 -> recipe.strIngredient18
                                19 -> recipe.strIngredient19
                                20 -> recipe.strIngredient20
                                else -> null
                            }
                            val measure = when (i) {
                                1 -> recipe.strMeasure1
                                2 -> recipe.strMeasure2
                                3 -> recipe.strMeasure3
                                4 -> recipe.strMeasure4
                                5 -> recipe.strMeasure5
                                6 -> recipe.strMeasure6
                                7 -> recipe.strMeasure7
                                8 -> recipe.strMeasure8
                                9 -> recipe.strMeasure9
                                10 -> recipe.strMeasure10
                                11 -> recipe.strMeasure11
                                12 -> recipe.strMeasure12
                                13 -> recipe.strMeasure13
                                14 -> recipe.strMeasure14
                                15 -> recipe.strMeasure15
                                16 -> recipe.strMeasure16
                                17 -> recipe.strMeasure17
                                18 -> recipe.strMeasure18
                                19 -> recipe.strMeasure19
                                20 -> recipe.strMeasure20
                                else -> null
                            }
                            if (!ingredient.isNullOrBlank() && !measure.isNullOrBlank()) {
                                Text(
                                    text = "- $measure $ingredient",
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }else {
                    Text(
                        text = "Recipe not found",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}
