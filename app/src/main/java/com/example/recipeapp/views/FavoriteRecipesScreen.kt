package com.example.recipeapp.views


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.viewmodels.FavoritesViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FavoriteRecipesScreen(favoritesViewModel: FavoritesViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var favoriteRecipes by remember { mutableStateOf<List<Triple<String, String, String>>?>(null) }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val recipes = favoritesViewModel.getFavorites(uid)
            favoriteRecipes = recipes
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (favoriteRecipes == null) {
            item {
                CircularProgressIndicator(Modifier.fillParentMaxSize().padding(vertical = 16.dp))
            }
        } else if (favoriteRecipes?.isEmpty() == true) {
            item {
                Text(
                    text = "No favorite recipes found",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }
        } else {
            items(favoriteRecipes ?: emptyList()) { recipeTriple ->
                FavoriteRecipeItem(recipeTriple = recipeTriple)
            }
        }
    }
}

@Composable
fun FavoriteRecipeItem(recipeTriple: Triple<String, String, String>) {
    val (recipeId, category, thumbnailUrl) = recipeTriple

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(thumbnailUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Recipe: $recipeId",
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(
            text = "Category: $category",
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}