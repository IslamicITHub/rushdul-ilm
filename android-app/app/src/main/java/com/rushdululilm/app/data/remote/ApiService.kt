// File: ApiService.kt
// Purpose: Defines the Retrofit interface for the Rushd-ul-Ilm backend API.
// Layer: 4 — Connect Android to Backend
// Created: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 📜 This interface acts as the "Menu" for our API.
// Retrofit will implement these functions for us automatically.
interface ApiService {

    // 🏥 A simple health check to see if the server is awake.
    // @GET means we are just "getting" information without sending much.
    @GET("health")
    suspend fun checkHealth(): Response<Map<String, String>>

    // 🤖 The main endpoint to ask Islamic questions.
    // @POST means we are "posting" (sending) a request body to the server.
    // 'suspend' means this function runs in the background so the UI doesn't freeze.
    @POST("query")
    suspend fun askQuestion(
        @Body request: QueryRequest
    ): Response<QueryResponse>
}
