package com.example.recipeapp.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSaveProfile: (String) -> Unit
) {
    var nameState by remember { mutableStateOf(currentName) }
    var isNameValid by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Let's get to know you!") },
        text = {
            Column {
                Text("Please enter your name.")
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = nameState,
                    onValueChange = {
                        nameState = it
                        val validationResult = validateName(it)
                        isNameValid = validationResult.isValid
                        errorMessage = validationResult.errorMessage
                    },
                    label = { Text("Name") }
                )
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveProfile(nameState)
                    onDismiss()
                },
                enabled = isNameValid // Enable button based on validation result
            ) {
                Text("Save")
            }
        }
    )
}

// Validation logic for name
data class ValidationResult(val isValid: Boolean, val errorMessage: String)

fun validateName(name: String): ValidationResult {
    val errorMessage = when {
        name.isBlank() -> "Name cannot be empty"
        name.length > 20 -> "Name cannot exceed 20 characters"
        !name.all { it.isLetter() } -> "Name can only contain letters"
        else -> ""
    }
    return ValidationResult(errorMessage.isEmpty(), errorMessage)
}
