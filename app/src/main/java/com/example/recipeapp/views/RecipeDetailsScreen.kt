package com.example.recipeapp.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.data.Note
import com.example.recipeapp.data.RecipeDetails
import com.example.recipeapp.viewmodels.FavoritesViewModel
import com.example.recipeapp.viewmodels.ProfileViewModel
import com.example.recipeapp.viewmodels.RecipeDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navigateToFavoriteRecipes: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val recipeDetailState by viewModel.recipeDetailState.observeAsState(
        initial = RecipeDetailViewModel.RecipeDetailState(loading = true)
    )
    val recipe = recipeDetailState.recipe

    var isFavorite by remember { mutableStateOf(false) }
    var loadingProfile by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()

    val recipeRating by viewModel.recipeRating.observeAsState()
    var userRating by remember { mutableStateOf(0) }

    // Fetch data when recipeId changes
    LaunchedEffect(recipeId) {
        if (recipeId.isNotBlank()) {
            viewModel.getRecipeDetails(recipeId)
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                viewModel.updateRecipeRating(recipeId)
                viewModel.getNotesForRecipeAndUser(recipeId, userId)
            }
        } else {
            Log.e("RecipeDetailScreen", "Invalid recipeId: $recipeId")
        }
    }

    // Observe existing notes
    val existingNotes by viewModel.existingNotes.observeAsState(emptyList())

    // Check if recipe is favorite
    LaunchedEffect(recipeId, recipe) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && recipe != null) {
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
    val userProfile by profileViewModel.userProfile.observeAsState()

    // Observe loading state for profile
    loadingProfile = userProfile == null

    // Dialog state for editing notes
    var editingNote by remember { mutableStateOf<Note?>(null) }
    // Track changes to editingNote
    LaunchedEffect(editingNote) {
        Log.d("RecipeDetailScreen", "Editing note: $editingNote")
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
                val profile = userProfile
                val allergies = profile?.allergies
                if (allergies?.isEmpty()!!) {
                    "You haven't specified any allergies."
                } else {
                    val hasAllergies = recipe?.let { recipeDetails ->
                        (1..20).any { i ->
                            val ingredient = recipeDetails.getIngredient(i)
                            ingredient != null && allergies.contains(ingredient)
                        }
                    } ?: false

                    if (hasAllergies) {
                        "You have allergies to some ingredients in this recipe."
                    } else {
                        "You don't have any allergies to ingredients in this recipe."
                    }
                }
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

                            // Text field for adding notes
                            TextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Add Notes") },
                                modifier = Modifier
                                    .focusRequester(focusRequester) // Attach the FocusRequester to the TextField
                                    .fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val noteId = UUID.randomUUID().toString()
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                                        val note = userId?.let {
                                            Note(
                                                noteId = noteId,
                                                recipeId = recipeId,
                                                userId = it,
                                                content = notes
                                            )
                                        }
                                        if (note != null && notes.isNotBlank()) {
                                            viewModel.addNote(note, userId)
                                            notes = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 16.dp),
                                enabled = notes.isNotBlank() // Disable if notes field is empty
                            ) {
                                Text("Save Notes")
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid
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
                            // Ratings UI
                            Spacer(modifier = Modifier.height(16.dp))
                            RatingsSection(
                                userRating = userRating,
                                averageRating = recipeRating?.first ?: 0.0,
                                ratingCount = recipeRating?.second ?: 0,
                                onRatingSelected = { rating ->
                                    userRating = rating
                                    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                                        if (recipe != null) {
                                            viewModel.addRating(recipe.idMeal, userId, rating)
                                        }
                                    }
                                }
                            )
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
            // Display existing notes or message if there are no notes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (existingNotes.isEmpty()) {
                    Text(
                        text = "You have not yet added any notes.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn {
                        items(existingNotes) { note ->
                            var isEditing by remember { mutableStateOf(false) }
                            var editedNote by remember { mutableStateOf(note.content) }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                if (isEditing) {
                                    // Editable TextField for note editing
                                    TextField(
                                        value = editedNote,
                                        onValueChange = { editedNote = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Edit note") }
                                    )

                                    // Save button for editing
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (editedNote.isNotBlank()) { // Ensure edited note is not empty
                                                    viewModel.updateNote(
                                                        noteId = note.noteId,
                                                        updatedContent = Note(
                                                            noteId = note.noteId,
                                                            recipeId = note.recipeId,
                                                            userId = note.userId,
                                                            content = editedNote
                                                        ),
                                                        userId = FirebaseAuth.getInstance().currentUser?.uid
                                                            ?: ""
                                                    )
                                                }
                                            }
                                            isEditing = false // Exit editing mode after saving
                                        },
                                        modifier = Modifier.padding(top = 16.dp),
                                        enabled = editedNote.isNotBlank() // Disable if edited note is empty
                                    ) {
                                        Text("Save")
                                    }
                                } else {
                                    // Display the note text
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = note.content,
                                            modifier = Modifier.weight(1f),
                                        )

                                        // Edit icon
                                        IconButton(
                                            onClick = { isEditing = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Note"
                                            )
                                        }

                                        // Delete icon
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    // Delete the note from Firestore
                                                    viewModel.deleteNoteById(
                                                        noteId = note.noteId,
                                                        userId = note.userId
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Note",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    color = Color.White
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

// Rating Section UI
@Composable
fun RatingsSection(
    userRating: Int,
    averageRating: Double,
    ratingCount: Int,
    onRatingSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Average Rating: ${"%.1f".format(averageRating)} ($ratingCount ratings)",
            style = TextStyle(fontWeight = FontWeight.Bold)
        )

        // Rating stars for user input
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            for (i in 1..5) {
                IconButton(onClick = { onRatingSelected(i) }) {
                    Icon(
                        imageVector = if (i <= userRating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Rating Star",
                        tint = Color.Yellow
                    )
                }
            }
        }
    }
}//make stars and stuff prettier?