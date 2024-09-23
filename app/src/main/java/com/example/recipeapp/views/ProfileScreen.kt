package com.example.recipeapp.views


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recipeapp.data.UserProfile
import com.example.recipeapp.viewmodels.IngredientsViewModel
import com.example.recipeapp.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: String,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    ingredientsViewModel: IngredientsViewModel = hiltViewModel()
) {

    val userProfile by profileViewModel.userProfile.observeAsState()
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var showDialogEditAllergies by remember { mutableStateOf(false) } // State for showing the edit allergies dialog
    var isSavingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile(userId)
    }

    LaunchedEffect(profileViewModel.isLoading) {
        profileViewModel.isLoading.observeForever {
            isLoading = it
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Display loading indicator if data is still loading
            CircularProgressIndicator()
        } else {
            if (userProfile == null || userProfile?.name.isNullOrEmpty()) {
                // If userProfile is null, display the dialog to enter the name
                showDialog = true
            } else {
                // Display profile content once data is loaded
                ProfileContent(
                    userProfile = userProfile!!,
                    onProfileSaved = {
                        isSavingProfile = true
                        profileViewModel.updateUserAllergies(userId, userProfile!!.allergies)
                    },
                    onEditNameClicked = { showDialog = true },
                    // Show the edit allergies dialog when clicked
                    onEditAllergiesClicked = { showDialogEditAllergies = true }
                )
            }
        }
    }

    // ProfileScreen
    if (showDialog) {
        ProfileDialog(
            currentName = userProfile?.name ?: "",  // Pass the current name
            onDismiss = { showDialog = false },
            onSaveProfile = { name ->
                if (userProfile == null || userProfile?.name.isNullOrEmpty()) {
                    // New user: Add profile data
                    profileViewModel.addProfileData(userId, name) {
                        showDialog = false
                    }
                } else {
                    // Existing user: Update the name
                    profileViewModel.updateUserName(userId, name) {
                        showDialog = false
                    }
                }
            }
        )
    }

    // Dialog to edit allergies
    if (showDialogEditAllergies) {
        // Fetch ingredients only when the dialog is opened
        LaunchedEffect(Unit) {
            ingredientsViewModel.fetchIngredients()
        }

        // Ensure that ingredients data is available before displaying the dialog
        val ingredients by ingredientsViewModel.ingredients.observeAsState()

        if (ingredients != null) {
            AllergiesDialog(
                selectedAllergies = userProfile?.allergies ?: emptyList(),
                onDismiss = { showDialogEditAllergies = false },
                onSaveAllergies = { allergies ->
                    profileViewModel.updateUserAllergies(userId, allergies)
                    showDialogEditAllergies = false
                },
                ingredientsViewModel = ingredientsViewModel,
                profileViewModel = profileViewModel,
                userId = userId
            )
        } else {
            // Display a progress indicator while ingredients are loading
            CircularProgressIndicator()
        }
    }

    // Show saving indicator
    if (isSavingProfile) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ProfileContent(
    userProfile: UserProfile,
    onProfileSaved: () -> Unit,
    onEditNameClicked: () -> Unit,
    onEditAllergiesClicked: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display user profile data
        Text("Name: ${userProfile.name}")
        Text("Allergies: ${userProfile.allergies.joinToString()}")

        // Button to edit profile name
        Button(
            onClick = { onEditNameClicked() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Edit Name")
        }

        // Button to edit allergies
        Button(
            onClick = { onEditAllergiesClicked() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Edit Allergies")
        }
    }
}