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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipeapp.data.UserProfile
import com.example.recipeapp.repositories.FirestoreRepository
import com.example.recipeapp.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    userId: String,
    profileViewModel: ProfileViewModel,
    firestoreRepository: FirestoreRepository
) {
    var isLoading by remember { mutableStateOf(true) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile(userId)
    }

    LaunchedEffect(profileViewModel.userProfile) {
        profileViewModel.userProfile.observeForever {
            userProfile = it
            isLoading = false
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
            // Display profile content once data is loaded
            userProfile?.let { userProfile ->
                ProfileContent(
                    userProfile = userProfile,
                    onProfileSaved = {
                        isSavingProfile = true
                        profileViewModel.addProfileData(userId, userProfile) {
                            isSavingProfile = false
                            userProfile.name = userName // Update userProfile directly
                        }
                    },
                    onEditNameClicked = { showDialog = true }
                )
            } ?: run {
                // Display a button to prompt user to enter their name
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Enter Your Name")
                }
            }
        }
    }

    // Dialog to prompt user to enter their name or edit their existing name
    if (showDialog) {
        ProfileDialog(
            currentName = userProfile?.name ?: "", // Pass the current name
            onDismiss = { showDialog = false },
            onSaveProfile = { name ->
                // Save user profile with entered name
                userName = name
                profileViewModel.addProfileData(userId, UserProfile(name = name)) {
                    showDialog = false
                    userProfile = UserProfile(name = name) // Update userProfile directly
                }
            }
        )
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
    onEditNameClicked: () -> Unit
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
    }
}