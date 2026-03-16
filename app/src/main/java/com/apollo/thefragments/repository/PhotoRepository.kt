package com.apollo.thefragments.repository

import com.apollo.thefragments.data.db.PhotoDao
import com.apollo.thefragments.data.model.CloudPhoto
import com.apollo.thefragments.data.model.Photo
import com.apollo.thefragments.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.coroutines.resume

class PhotoRepository(private val photoDao: PhotoDao) {

    private val CLOUD_NAME    = "dn6rffrwk"
    private val UPLOAD_PRESET = "TheFragments_CDN"
    private val UPLOAD_URL    = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseDatabase.getInstance().reference

    // ── Local ──────────────────────────────────────────────────────────────────

    fun getAllPhotos(): Flow<List<Photo>> = photoDao.getAllPhotos()

    suspend fun insertPhoto(photo: Photo) = photoDao.insertPhoto(photo)

    // ── Upload to Cloudinary via Retrofit ─────────────────────────────────────

    suspend fun uploadToCloudinary(photo: Photo): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(photo.localPath)
                if (!file.exists()) return@withContext Result.failure(Exception("File not found"))

                // Wrap each field as a RequestBody — Retrofit needs this for @Multipart
                val uploadPresetBody = UPLOAD_PRESET
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val publicIdBody = photo.id
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                // Wrap the image file as a MultipartBody.Part
                // "file" must match the field name Cloudinary expects
                val fileBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, fileBody)

                // Make the Retrofit call — suspend so it runs on Dispatchers.IO
                val response = RetrofitClient.cloudinaryApi.uploadImage(
                    url           = UPLOAD_URL,
                    uploadPreset  = uploadPresetBody,
                    publicId      = publicIdBody,
                    file          = filePart
                )

                Result.success(response.secure_url)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ── Save URL to Firebase RTDB ─────────────────────────────────────────────

    suspend fun saveToFirebase(photo: Photo, cloudUrl: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not logged in"))

            val cloudPhoto = CloudPhoto(
                id        = photo.id,
                cloudUrl  = cloudUrl,
                createdAt = photo.createdAt
            )

            db.child("users").child(uid).child("photos").child(photo.id)
                .setValue(cloudPhoto)
                .await()

            photoDao.markAsSynced(photo.id, cloudUrl)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Fetch cloud photos from Firebase RTDB ─────────────────────────────────

    suspend fun fetchCloudPhotos(): Result<List<CloudPhoto>> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    cont.resume(Result.failure(Exception("Not logged in")))
                    return@suspendCancellableCoroutine
                }

                db.child("users").child(uid).child("photos")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val photos = snapshot.children.mapNotNull {
                                it.getValue(CloudPhoto::class.java)
                            }
                            cont.resume(Result.success(photos))
                        }
                        override fun onCancelled(error: DatabaseError) {
                            cont.resume(Result.failure(error.toException()))
                        }
                    })
            }
        }
    }
}