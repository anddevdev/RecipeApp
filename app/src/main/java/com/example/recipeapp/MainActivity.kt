package com.example.recipeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.api.RecipeDetailsApiService
import com.example.recipeapp.repositories.FirestoreRepository
import com.example.recipeapp.viewmodels.LoginViewModel
import com.example.recipeapp.viewmodels.RegistrationViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val loginViewModel: LoginViewModel = viewModel()
            val registrationViewModel: RegistrationViewModel = viewModel()
            val currentUser = FirebaseAuth.getInstance().currentUser



            RecipeApp(
                navController = navController,
                loginViewModel = loginViewModel,
                registrationViewModel = registrationViewModel,
                firestoreRepository = FirestoreRepository(RecipeDetailsApiService.instance),

            )


            // Observe changes in login status to display Toast messages
            val isLoggedOut by loginViewModel.isLoggedOut.observeAsState(initial = true)

            LaunchedEffect(isLoggedOut) {
                if (!isLoggedOut) {
                    showToast("Login successful")
                }
            }

            LaunchedEffect(isLoggedOut) {
                if (isLoggedOut) {
                    showToast("You are logged out")
                }
            }

            val isLoggedInAnonymously by loginViewModel.isLoggedInAnonymously.observeAsState(initial = false)

            LaunchedEffect(isLoggedInAnonymously) {
                if (isLoggedInAnonymously) {
                    showToast("You are logged in as anonymous user")
                }
            }

            val isRegistered by registrationViewModel.isRegistered.observeAsState(initial = false)

            LaunchedEffect(isRegistered) {
                if (isRegistered) {
                    showToast("Registration successful")
                }
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}




