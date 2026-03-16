package com.apollo.thefragments.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey
    val id: String,           // timestamp-based unique ID e.g. "20240101_123456"
    val localPath: String,    // absolute path to file on device
    val cloudUrl: String = "", // Cloudinary URL — empty if not synced yet
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)