package com.apollo.thefragments.ui.camera

import androidx.lifecycle.*
import com.apollo.thefragments.data.model.CloudPhoto
import com.apollo.thefragments.data.model.Photo
import com.apollo.thefragments.repository.PhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraViewModel(private val repo: PhotoRepository) : ViewModel() {

    // Local photos — auto-updates via Room Flow
    val photos: StateFlow<List<Photo>> = repo.getAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cloud photos fetched from Firebase (other-device login scenario)
    private val _cloudPhotos = MutableLiveData<List<CloudPhoto>>(emptyList())
    val cloudPhotos: LiveData<List<CloudPhoto>> = _cloudPhotos

    // Per-photo upload state: photoId → UploadState
    private val _uploadStates = MutableLiveData<Map<String, UploadState>>(emptyMap())
    val uploadStates: LiveData<Map<String, UploadState>> = _uploadStates

    // Toast messages
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // ── Insert new local photo ─────────────────────────────────────────────────

    fun insertPhoto(photo: Photo) {
        viewModelScope.launch { repo.insertPhoto(photo) }
    }

    // ── Sync single photo ──────────────────────────────────────────────────────

    fun syncPhoto(photo: Photo) {
        viewModelScope.launch {
            setUploadState(photo.id, UploadState.UPLOADING)

            // Step 1: Upload to Cloudinary
            val uploadResult = repo.uploadToCloudinary(photo)
            if (uploadResult.isFailure) {
                setUploadState(photo.id, UploadState.ERROR)
                _message.postValue("Upload failed: ${uploadResult.exceptionOrNull()?.message}")
                return@launch
            }

            // Step 2: Save URL to Firebase RTDB
            val cloudUrl = uploadResult.getOrThrow()
            val saveResult = repo.saveToFirebase(photo, cloudUrl)
            if (saveResult.isFailure) {
                setUploadState(photo.id, UploadState.ERROR)
                _message.postValue("Failed to save to Firebase: ${saveResult.exceptionOrNull()?.message}")
                return@launch
            }

            setUploadState(photo.id, UploadState.SYNCED)
            _message.postValue("Photo synced successfully!")
        }
    }

    // ── Fetch cloud photos (called on fragment start if online) ────────────────

    fun fetchCloudPhotos() {
        viewModelScope.launch {
            val result = repo.fetchCloudPhotos()
            if (result.isSuccess) {
                _cloudPhotos.postValue(result.getOrThrow())
            }
        }
    }

    private fun setUploadState(photoId: String, state: UploadState) {
        val current = _uploadStates.value?.toMutableMap() ?: mutableMapOf()
        current[photoId] = state
        _uploadStates.postValue(current)
    }

    fun clearMessage() { _message.value = null }
}

enum class UploadState {
    IDLE,       // not attempted yet
    UPLOADING,  // in progress
    SYNCED,     // done
    ERROR       // failed
}