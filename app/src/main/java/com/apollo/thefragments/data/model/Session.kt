package com.apollo.thefragments.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Single-row table — only ever has id = 1
// isLoggedIn → SplashActivity reads this to decide where to go
// provider   → "email" or "google" (for display/debugging)
@Entity(tableName = "session")
data class Session(
    @PrimaryKey
    val id: Int = 1,
    val isLoggedIn: Boolean,
    val provider: String = ""
)
