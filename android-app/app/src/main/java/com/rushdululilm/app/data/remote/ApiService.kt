// File: ApiService.kt
// Purpose: Defines the Retrofit interface for the Rushd-ul-Ilm backend API.
// Layer: Layer 1 — Android App (Data Remote)
// Depends on: NetworkModels.kt
// Created: 2026-06-08 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.remote

import retrofit2.Response
// ^ Retrofit class that wraps the HTTP response, giving access to the status code, headers, and body data
import retrofit2.http.Body
// ^ Retrofit annotation declaring that a parameter should be serialized as the HTTP request body payload
import retrofit2.http.GET
// ^ Retrofit annotation specifying an HTTP GET request to retrieve data from a server endpoint
import retrofit2.http.POST
// ^ Retrofit annotation specifying an HTTP POST request to send data to a server endpoint
import retrofit2.http.Url
// ^ Retrofit annotation to dynamically pass a full URL overriding the base URL

// 🏛️ CONCEPT: An interface in Kotlin defines a contract of functions without implementing their bodies.
//    Retrofit takes this interface and dynamically generates the actual HTTP client network code for us.
//    The 'suspend' keyword indicates that the function is a coroutine, meaning it pauses execution without freezing the app UI thread.
// 🏛️ ANALOGY: ApiService is like a waiter's order pad. 
//    You write down the request names (endpoints like "health" or "query"). Retrofit takes this pad to the kitchen (server) and brings back the food (response).
interface ApiService {
// ^ Declares an interface named ApiService for network calls

    @GET("health")
    // ^ Annotation mapping this function to an HTTP GET request at the "health" endpoint path
    suspend fun checkHealth(): Response<Map<String, String>>
    // ^ suspend function checkHealth executes a background call and returns an HTTP Response containing key-value string maps
    // ^ This checks if the local FastAPI backend server is online and running

    @POST("query")
    // ^ Annotation mapping this function to an HTTP POST request at the "query" endpoint path
    suspend fun askQuestion(
    // ^ suspend function askQuestion sends a query request payload and returns the answer response
        @Body request: QueryRequest
        // ^ @Body passes the serialized QueryRequest object (which contains the question and filters) in the request body
    ): Response<QueryResponse>
    // ^ Returns an HTTP Response wrapping a QueryResponse object (containing the AI answer and sources)

    @POST
    // ^ Annotation mapping this function to an HTTP POST request. We don't specify the path here because we use @Url below.
    suspend fun generateSpeech(
    // ^ suspend function to request audio speech generation for given text
        @Url url: String,
        // ^ @Url overrides the default Retrofit base URL and uses this dynamic URL string instead (used for hitting port 8002)
        @Body request: TTSRequest
        // ^ @Body passes the serialized TTSRequest object
    ): Response<TTSResponse>
    // ^ Returns an HTTP Response wrapping a TTSResponse object containing the base64 audio

    @retrofit2.http.Multipart
    // ^ Annotation stating that the request will have multiple parts (used for file uploads)
    @POST
    suspend fun transcribeAudio(
    // ^ suspend function to send recorded audio for speech-to-text transcription
        @Url url: String,
        // ^ @Url overrides the base URL to hit the STT service (port 8003)
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
        // ^ @Part sends the audio file as a multipart form data part
    ): Response<TranscriptionResponse>
    // ^ Returns an HTTP Response wrapping a TranscriptionResponse object with the transcribed text
}
// ^ Ends ApiService interface definition
