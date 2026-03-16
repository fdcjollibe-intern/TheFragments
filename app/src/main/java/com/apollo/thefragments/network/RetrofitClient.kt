package com.apollo.thefragments.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Logging interceptor — shows full request/response in Logcat (remove in production)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)  // wait 30s to connect
        .writeTimeout(60, TimeUnit.SECONDS)    // wait 60s to finish uploading
        .readTimeout(30, TimeUnit.SECONDS)     // wait 30s for response
        .build()

    // Base URL is just a placeholder — we pass the full URL via @Url in the interface
    val cloudinaryApi: CloudinaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApiService::class.java)
    }
}