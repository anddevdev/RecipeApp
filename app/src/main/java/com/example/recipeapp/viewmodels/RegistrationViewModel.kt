package com.example.recipeapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class RegistrationViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    val isRegistered = MutableLiveData<Boolean>()

    init {
        isRegistered.value = false
    }

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isRegistered.value = true
                    callback(true, null) // Registration successful
                } else {
                    callback(false, task.exception?.message) // Registration failed
                }
            }
    }
}
