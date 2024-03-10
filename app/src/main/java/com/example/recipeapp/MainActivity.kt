package com.example.recipeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
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

            var isLoggedOut by remember { mutableStateOf(true) }
            var isRegistered by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(false) }

            val currentUser = FirebaseAuth.getInstance().currentUser

            RecipeApp(
                navController = navController,
                isLoggedOut = isLoggedOut,
                isRegistered = isRegistered,
                loginViewModel = loginViewModel,
                registrationViewModel = registrationViewModel,
                isLoggedIn = isLoggedIn,
                firestoreRepository = FirestoreRepository(),
            )


            // Observe changes in states to display Toast messages
            loginViewModel.isLoggedOut.observe(this) { isLoggedOut ->
                if (!isLoggedOut) {
                    showToast("Login successful")
                }
            }

            loginViewModel.isLoggedOut.observe(this) { isLoggedOut ->
                if (isLoggedOut) {
                    showToast("You are logged out")
                }
            }

            loginViewModel.isLoggedInAnonymously.observe(this) { isLoggedInAnonymously ->
                if (isLoggedInAnonymously) {
                    showToast("You are logged in as anonymous user")
                }
            }

            registrationViewModel.isRegistered.observe(this) { isRegistered ->
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




