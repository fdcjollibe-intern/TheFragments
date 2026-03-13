package com.apollo.thefragments.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollo.thefragments.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Single LiveData for all auth results — observed by both Login and Register fragments
    // "SUCCESS" = navigate to MainActivity
    // anything else = show as error toast
    private val _authResult = MutableLiveData<String>()
    val authResult: LiveData<String> = _authResult

    // Tracks loading state — show/hide progress indicator
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = repository.registerWithEmail(email, password)
            _isLoading.postValue(false)
            if (result.isSuccess) {
                _authResult.postValue("SUCCESS")
            } else {
                // Firebase gives readable error messages (e.g. "The email address is already in use")
                _authResult.postValue(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = repository.loginWithEmail(email, password)
            _isLoading.postValue(false)
            if (result.isSuccess) {
                _authResult.postValue("SUCCESS")
            } else {
                _authResult.postValue(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val result = repository.loginWithGoogle(account)
            _isLoading.postValue(false)
            if (result.isSuccess) {
                _authResult.postValue("SUCCESS")
            } else {
                _authResult.postValue(result.exceptionOrNull()?.message ?: "Google sign-in failed")
            }
        }
    }
}
