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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.viewmodels.FavoritesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun FavoriteRecipesScreen(favoritesViewModel: FavoritesViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Collect the list of favorite recipes using State
    var favoriteRecipes by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(userId) {
        userId?.let {
            val recipes = favoritesViewModel.getFavorites(it)
            favoriteRecipes = recipes
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(favoriteRecipes) { recipeName ->
            // Display each favorite recipe name
            Text(text = recipeName)
        }
    }
}
