// File: NetworkModule.kt
// Purpose: Provides Retrofit and OkHttp instances using Hilt Dependency Injection.
// Layer: 4 — Connect Android to Backend
// Created: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.di

import com.rushdululilm.app.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This module lives as long as the app is open
object NetworkModule {

    // 🛠️ Provide the Base URL for the server.
    // Since we are running in an emulator, 10.0.2.2 points to our Ubuntu host's localhost.
    //private const val BASE_URL = "http://10.0.2.2:8000/"
    private const val BASE_URL = "http://192.168.0.102:8000/"
    @Provides
    @Singleton // Create only one instance and share it everywhere
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Set timeouts so the app doesn't wait forever if the server is slow.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // GsonConverterFactory automatically turns JSON into Kotlin objects.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        // This is where Retrofit actually "cooks" the ApiService implementation.
        return retrofit.create(ApiService::class.java)
    }
}
