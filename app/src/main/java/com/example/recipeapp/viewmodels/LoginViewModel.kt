package com.example.recipeapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val isLoggedOut = MutableLiveData<Boolean>()

    init {
        isLoggedOut.value = true
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
}
