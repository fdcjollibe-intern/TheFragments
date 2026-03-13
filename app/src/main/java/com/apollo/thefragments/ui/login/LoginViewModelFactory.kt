package com.apollo.thefragments.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.apollo.thefragments.repository.AuthRepository

// ─────────────────────────────────────────────────────────────
// WHY DO WE NEED A FACTORY?
// ─────────────────────────────────────────────────────────────
// Normally ViewModelProvider creates ViewModels by calling
// the constructor with NO arguments.
//
// But our LoginViewModel needs a repository passed in:
//   LoginViewModel(private val repository: AuthRepository)
//
// ViewModelProvider doesn't know how to do that on its own.
// So we give it a Factory — a helper that tells it exactly
// how to create our ViewModel with the right arguments.
// ─────────────────────────────────────────────────────────────
class LoginViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check that the requested ViewModel class is LoginViewModel
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
