package com.example.recipeapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.recipeapp.viewmodels.RegistrationViewModel

@Composable
fun RegisterScreen(
    onRegistrationSuccess: () -> Unit,
    viewModel: RegistrationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }

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
                isEmailValid = it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text("Email") },
            isError = !isEmailValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordValid = it.isNotBlank() && it.length >= 6 // Example: Minimum 6 characters
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = !isPasswordValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(it, modifier = Modifier.padding(vertical = 8.dp), color = androidx.compose.ui.graphics.Color.Red)
        }
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && isEmailValid && isPasswordValid) {
                    viewModel.register(email, password) { isSuccess, errorMsg ->
                        if (!isSuccess) {
                            errorMessage = errorMsg ?: "Registration failed"
                        } else {
                            onRegistrationSuccess()
                        }
                    }
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && isEmailValid && isPasswordValid
        ) {
            Text("Register")
        }
    }
}
