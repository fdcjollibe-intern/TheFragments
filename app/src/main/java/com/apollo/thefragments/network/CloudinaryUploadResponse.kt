package com.apollo.thefragments.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

// The response Cloudinary sends back after a successful upload
data class CloudinaryUploadResponse(
    val secure_url: String = "",
    val public_id: String  = ""
)

interface CloudinaryApiService {

    // @Url lets us pass the full URL at call time — useful since the URL
    // contains the cloud name (dn6rffrwk) which could change later
    @Multipart
    @POST
    suspend fun uploadImage(
        @Url url: String,
        @Part("upload_preset") uploadPreset: RequestBody,
        @Part("public_id")     publicId: RequestBody,
        @Part file:            MultipartBody.Part
    ): CloudinaryUploadResponse
}