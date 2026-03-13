package com.apollo.thefragments.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Stores the Firebase user info locally after login
// uid = Firebase user ID (unique per Firebase account)
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String
)
