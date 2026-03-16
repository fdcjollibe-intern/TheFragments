package com.apollo.thefragments.data.model

// Used when fetching photos from Firebase Realtime Database.
// Firebase needs a no-arg constructor → default values on all fields.
data class CloudPhoto(
    val id: String = "",
    val cloudUrl: String = "",
    val createdAt: Long = 0L
)