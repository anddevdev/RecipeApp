package com.example.recipeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.viewmodels.LoginViewModel
import com.example.recipeapp.viewmodels.RegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            RecipeApp(navController = navController)

            // Observe changes in login status to display Toast messages
            val loginViewModel: LoginViewModel = hiltViewModel()
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

            val registrationViewModel: RegistrationViewModel = hiltViewModel()
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




