package com.example.recipeapp.views


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.recipeapp.data.Recipe
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.data.UserProfile
import com.example.recipeapp.viewmodels.FavoritesViewModel
import com.example.recipeapp.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    recipe: Recipe,
    favoritesViewModel: FavoritesViewModel,
    profileViewModel: ProfileViewModel,
    navigateToFavoriteRecipes: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    var loadingProfile by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    val recipeDetailState = viewModel.recipeDetailState.value

    LaunchedEffect(recipe) {
        viewModel.fetchRecipeDetailsIfNeeded(recipe.strMeal)
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val favorites = favoritesViewModel.getFavorites(userId)
            isFavorite = favorites.any { it.first == recipe.strMeal }
        }
    }

    // Fetch user allergies when the screen is first displayed
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            profileViewModel.getUserProfile(userId) // Fetch user profile including allergies
        }
    }

    // Observe changes in user profile (including allergies)
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    profileViewModel.userProfile.observeForever {
        userProfile = it
        loadingProfile = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Allergy Check")
            val allergiesText = if (loadingProfile) {
                "Loading..."
            } else if (userProfile == null) {
                "Your profile is empty."
            } else {
                userProfile?.let { profile ->
                    val allergies = profile.allergies
                    if (allergies.isEmpty()) {
                        "You haven't specified any allergies."
                    } else {
                        val hasAllergies = recipeDetailState?.recipe?.let { recipe ->
                            (1..20).any { i ->
                                val ingredient = recipe.getIngredient(i)
                                ingredient != null && allergies.contains(ingredient)
                            }
                        } ?: false

                        if (hasAllergies) {
                            "You have allergies to some ingredients in this recipe."
                        } else {
                            "You don't have any allergies to ingredients in this recipe."
                        }
                    }
                } ?: ""
            }

            val textColor = when {
                allergiesText.contains("You have allergies") -> Color.Red
                allergiesText.contains("You don't have any allergies") -> Color.Green
                else -> Color.Black
            }

            Text(
                text = allergiesText,
                modifier = Modifier.padding(vertical = 8.dp),
                style = TextStyle(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        item {
            recipeDetailState.let {
                if (it.loading) {
                    // Show loading indicator
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (it.error != null) {
                    // Show error message
                    Text(
                        text = "Error: ${it.error}",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    )
                } else {
                    // Show recipe details
                    val recipe = it.recipe
                    if (recipe != null) {
                        Column {
                            Text(
                                text = recipe.strMeal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            Image(
                                painter = rememberAsyncImagePainter(recipe.strMealThumb),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(shape = RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Display recipe instructions
                            Text(
                                text = "Instructions:",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            Text(
                                text = recipe.strInstructions,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )

                            // Display ingredients and measures
                            Text(
                                text = "Ingredients:",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            for (i in 1..20) { // Assuming maximum 20 ingredients
                                val ingredient = recipe.getIngredient(i)
                                val measure = recipe.getMeasure(i)
                                if (!ingredient.isNullOrBlank() && !measure.isNullOrBlank()) {
                                    Text(
                                        text = "- $measure $ingredient",
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val userId =
                                            FirebaseAuth.getInstance().currentUser?.uid
                                        if (userId != null) {
                                            if (isFavorite) {
                                                favoritesViewModel.removeFavorite(
                                                    userId,
                                                    recipe.strMeal
                                                )
                                            } else {
                                                favoritesViewModel.addFavorite(
                                                    userId,
                                                    recipe.strMeal,
                                                    recipe.strMealThumb,
                                                    recipe.strCategory
                                                )
                                            }
                                            isFavorite = !isFavorite
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    if (isFavorite) "Remove from Favorites"
                                    else "Add to Favorites",
                                )
                            }
                        }
                    } else {
                        // Show recipe not found message
                        Text(
                            text = "Recipe not found",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        )
                    }
                }
            }
        }
        item {
            // Button to navigate to favorite recipes screen
            Button(
                onClick = { navigateToFavoriteRecipes() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Go to Favorite Recipes",
                    style = TextStyle(fontWeight = FontWeight.Bold), // Set text style here
                    color = Color.White // Set text color here
                )
            }
        }
    }
}


// Extension functions for RecipeDetails to access ingredients and measures
fun RecipeDetails.getIngredient(index: Int): String? {
    return when (index) {
        1 -> strIngredient1
        2 -> strIngredient2
        3 -> strIngredient3
        4 -> strIngredient4
        5 -> strIngredient5
        6 -> strIngredient6
        7 -> strIngredient7
        8 -> strIngredient8
        9 -> strIngredient9
        10 -> strIngredient10
        11 -> strIngredient11
        12 -> strIngredient12
        13 -> strIngredient13
        14 -> strIngredient14
        15 -> strIngredient15
        16 -> strIngredient16
        17 -> strIngredient17
        18 -> strIngredient18
        19 -> strIngredient19
        20 -> strIngredient20
        else -> null
    }
}

fun RecipeDetails.getMeasure(index: Int): String? {
    return when (index) {
        1 -> strMeasure1
        2 -> strMeasure2
        3 -> strMeasure3
        4 -> strMeasure4
        5 -> strMeasure5
        6 -> strMeasure6
        7 -> strMeasure7
        8 -> strMeasure8
        9 -> strMeasure9
        10 -> strMeasure10
        11 -> strMeasure11
        12 -> strMeasure12
        13 -> strMeasure13
        14 -> strMeasure14
        15 -> strMeasure15
        16 -> strMeasure16
        17 -> strMeasure17
        18 -> strMeasure18
        19 -> strMeasure19
        20 -> strMeasure20
        else -> null
    }
}


