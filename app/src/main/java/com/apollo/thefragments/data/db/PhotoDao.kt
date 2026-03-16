package com.apollo.thefragments.data.db

import androidx.room.*
import com.apollo.thefragments.data.model.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    // Flow so CameraFragment auto-updates when data changes
    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("UPDATE photos SET cloudUrl = :url, isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String, url: String)

    @Query("SELECT * FROM photos WHERE isSynced = 0")
    suspend fun getUnsyncedPhotos(): List<Photo>

    @Query("DELETE FROM photos")
    suspend fun clearAll()

    // One-shot suspend query — used by PhotoViewerActivity (not a Flow)
    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    suspend fun getAllPhotosOnce(): List<Photo>


}