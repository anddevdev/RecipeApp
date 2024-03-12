package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val isLoggedOut = MutableLiveData<Boolean>()
    val isLoggedInAnonymously = MutableLiveData<Boolean>()


    init {
        isLoggedOut.value = true
        isLoggedInAnonymously.value = false
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLoggedOut.value = false
                    callback(true, null) // Login successful
                } else {
                    callback(false, task.exception?.message) // Login failed
                }
            }
    }

    fun signInAnonymously(callback: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLoggedInAnonymously.value = true
                    callback(true, null) // Sign in success
                    Log.d("RecipeApp", "isLoggedInAnonymously: ${isLoggedInAnonymously.value}")
                } else {
                    callback(false, task.exception?.message) // Sign in failed
                }
            }
    }

    fun logout(registrationViewModel: RegistrationViewModel) {
        auth.signOut()
        isLoggedOut.value = true
        isLoggedInAnonymously.value = false
        registrationViewModel.isRegistered.value = false
    }

}

