package com.example.recipeapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeapp.data.UserProfile
import com.example.recipeapp.repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?>
        get() = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // Get user profile data
    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true // Set loading state to true
                Log.d("ProfileViewModel", "Is user profile loading?: ${_isLoading.value}")
                val userProfile = repository.getUserProfile(userId)
                _userProfile.value = userProfile
                Log.d("ProfileViewModel", "User profile retrieved successfully: $userProfile")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error getting user profile: ${e.message}")
            } finally {
                _isLoading.value = false // Set loading state to false when done
                Log.d("ProfileViewModel", "Is user profile loading?: ${_isLoading.value}")
            }
        }
    }


    fun updateUserName(userId: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateUserName(userId, name)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating username: ${e.message}")
            }
        }
    }

    fun updateUserAllergies(userId: String, allergies: List<String>) {
        viewModelScope.launch {
            try {
                repository.addUserAllergies(userId, allergies)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating allergies: ${e.message}")
            }
        }
    }

    fun addProfileData(userId: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userProfile = repository.getUserProfile(userId)
                if (userProfile == null || userProfile.name.isNullOrEmpty()) {
                    repository.addProfileData(userId, UserProfile(name = name))
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error adding profile data: ${e.message}")
            }
        }
    }


}



