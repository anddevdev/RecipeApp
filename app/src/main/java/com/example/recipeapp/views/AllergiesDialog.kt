package com.example.recipeapp.views

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.recipeapp.data.Ingredient
import com.example.recipeapp.viewmodels.IngredientsViewModel
import com.example.recipeapp.viewmodels.ProfileViewModel

@Composable
fun AllergiesDialog(
    selectedAllergies: List<String>,
    onDismiss: () -> Unit,
    onSaveAllergies: (allergies: List<String>) -> Unit,
    ingredientsViewModel: IngredientsViewModel,
    profileViewModel: ProfileViewModel,
    userId: String
) {
    // Initialize a remember state to store selected ingredients
    val selectedIngredients = remember { mutableStateOf(selectedAllergies.toMutableSet()) }

    // Observe the ingredients LiveData
    val ingredients by ingredientsViewModel.ingredients.observeAsState()

    // Fetch ingredients if not already fetched
    LaunchedEffect(ingredients) {
        if (ingredients == null) {
            ingredientsViewModel.fetchIngredients()
        }
    }

    // Filtered list of ingredients based on search query
    var filteredIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }

    // Search query state
    var searchQuery by remember { mutableStateOf("") }

    DisposableEffect(ingredients) {//To check if data is cleared properly
        Log.d("IngredientsLiveData", "Ingredients LiveData value: $ingredients")
        onDispose { }
    }

    if (ingredients != null) {
        Dialog(
            onDismissRequest = {
                onDismiss()
                // Clear ingredients data when dialog is dismissed
                ingredientsViewModel.clearIngredients()
            },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            )
        ) {
            Surface(
                modifier = Modifier.width(300.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Search TextField
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Filter ingredients based on search query
                    filteredIngredients = ingredients!!.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }

                    Text("Select Allergies")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredIngredients) { ingredient ->
                            CheckboxListItem(
                                text = ingredient.name,
                                isChecked = selectedIngredients.value.contains(ingredient.name),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedIngredients.value.add(ingredient.name)
                                    } else {
                                        selectedIngredients.value.remove(ingredient.name)
                                    }
                                    // Trigger recomposition(not needed probably)
                                    selectedIngredients.value = HashSet(selectedIngredients.value)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { onDismiss() }, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSaveAllergies(selectedIngredients.value.toList())
                                profileViewModel.updateUserAllergies(
                                    userId,
                                    selectedIngredients.value.toList()
                                )
                                onDismiss() // Dismiss the dialog after saving
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    } else {
        CircularProgressIndicator()
    }
}

@Composable
fun CheckboxListItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(text)
    }
}

/* TODO:
    Fix the real-time display of checkmarks.
      add warning that user should review this list fully and carefully
      clear data when it is not used (probably not really necessary)*/