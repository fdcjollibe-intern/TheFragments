package com.apollo.thefragments.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.apollo.thefragments.repository.PhotoRepository

class CameraViewModelFactory(private val repo: PhotoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}