package com.apollo.thefragments.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollo.thefragments.repository.AuthRepository
import kotlinx.coroutines.launch

// ViewModel holds the logic for the login screen.
// The Activity just observes the results and updates the UI.
// This keeps the Activity clean — no business logic in there.
class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // LiveData — the Activity observes these values.
    // When we post a new value here, the Activity automatically
    // reacts and updates the UI (show toast, navigate, etc.)
    //
    // MutableLiveData = we can write to it (private, inside ViewModel only)
    // LiveData        = read-only (exposed to the Activity)
    // ─────────────────────────────────────────────────────────────
    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> = _loginResult

    private val _registerResult = MutableLiveData<String>()
    val registerResult: LiveData<String> = _registerResult

    // Called when user taps Login button
    fun login(username: String, password: String) {
        // viewModelScope — coroutine tied to this ViewModel's lifetime
        // When the ViewModel is cleared, this coroutine is cancelled automatically
        viewModelScope.launch {
            // Input validation — basic check before hitting the database
            if (username.isBlank() || password.isBlank()) {
                _loginResult.postValue("Username and password cannot be empty")
                return@launch
            }

            val user = repository.login(username, password)
            if (user != null) {
                // User found in database — login success
                _loginResult.postValue("SUCCESS")
            } else {
                // No matching username + password in database
                _loginResult.postValue("Invalid username or password")
            }
        }
    }

    // Called when user taps Register button
    fun register(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _registerResult.postValue("Username and password cannot be empty")
                return@launch
            }

            val result = repository.register(username, password)
            if (result.isSuccess) {
                _registerResult.postValue("SUCCESS")
            } else {
                // Pass the error message (e.g. "Username already exists") to the Activity
                _registerResult.postValue(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}