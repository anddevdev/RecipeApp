package com.example.recipeapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.recipeapp.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                    null
                } else {
                    "Invalid email address"
                }
            },
            label = { Text("Email") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        emailError?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.isNotBlank() && it.length >= 6) {
                    null
                } else {
                    "Password must be at least 6 characters"
                }
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        passwordError?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null) {
                    viewModel.login(email, password) { isSuccess, errorMsg ->
                        if (!isSuccess) {
                            errorMessage = errorMsg ?: "Login failed"
                        } else {
                            onLoginSuccess()
                        }
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.signInAnonymously { isSuccess, errorMsg ->
            if (isSuccess) {
                onLoginSuccess()
            } else {
                errorMessage = errorMsg ?: "Anonymous login failed"
            }
        }}) {
            Text("Login anonymously")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRegisterClick) {
            Text("Register new account")
        }
    }
}