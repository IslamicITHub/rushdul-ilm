// File: NetworkModels.kt
// Purpose: Defines the JSON data structures for communication with the FastAPI backend.
// Layer: Layer 1 — Android App (Data Remote)
// Depends on: None (Standalone Data Blueprints)
// Created: 2026-06-08 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.remote

import com.google.gson.annotations.SerializedName
// ^ Gson library annotation that tells Retrofit the exact JSON key name to map to this Kotlin variable

// 🏛️ CONCEPT: Network data models define the structure of JSON data exchanged over HTTP.
//    @SerializedName maps Kotlin camelCase property names to backend JSON snake_case keys.
//    Making properties nullable (e.g., String?) protects the app from crashing if fields are missing in responses.
// 🏛️ ANALOGY: NetworkModels are like standardized mailing label templates. 
//    They ensure that when we send or receive a package (JSON payload), the labels line up exactly with what the postal carrier (server) expects.

data class QueryRequest(
// ^ data class representing the payload sent in HTTP POST to the backend /query endpoint
    @SerializedName("question") val question: String,
    // ^ Serializes the 'question' property to the JSON key "question"
    // ^ val declares a read-only string containing the user's speech-to-text question
    
    @SerializedName("chat_history") val chatHistory: List<ChatMessage> = emptyList(),
    // ^ Serializes the 'chatHistory' property to the JSON key "chat_history"
    // ^ val declares a read-only list of ChatMessage objects to provide context, defaulting to an empty list
    
    @SerializedName("sources") val sources: List<String>? = null,
    // ^ Serializes the 'sources' property to the JSON key "sources"
    // ^ val declares a nullable read-only list of source names (e.g. ["deoband"]) to filter the RAG database search

    @SerializedName("language") val language: String? = null
    // ^ Serializes the 'language' property to the JSON key "language"
    // ^ val declares a nullable read-only string containing the language tag (e.g. "te") for translating queries
)
// ^ Ends QueryRequest class definition

data class ChatMessage(
// ^ data class representing a single message block in a multi-turn conversation
    @SerializedName("role") val role: String, 
    // ^ Serializes the 'role' property to the JSON key "role" (value can be "user" or "assistant")
    
    @SerializedName("content") val content: String 
    // ^ Serializes the 'content' property to the JSON key "content" (holds the message text)
)
// ^ Ends ChatMessage class definition

data class QueryResponse(
// ^ data class representing the backend server response containing the RAG search results
    @SerializedName("answer") val answer: String? = null,
    // ^ Serializes 'answer' to the JSON key "answer". Nullable String represents the generated fatwa answer text.
    
    @SerializedName("question") val question: String? = null,
    // ^ Serializes 'question' to the JSON key "question". Nullable String represents the question string recognized by the server.
    
    @SerializedName("expanded_search_query") val expandedSearchQuery: String? = null,
    // ^ Serializes 'expandedSearchQuery' to the JSON key "expanded_search_query". Nullable String represents the expanded search terms.
    
    @SerializedName("sources") val sources: List<String>? = null,
    // ^ Serializes 'sources' to the JSON key "sources". Nullable list of strings represents citation source URLs.
    
    @SerializedName("is_clarification") val isClarification: Boolean? = false,
    // ^ Serializes 'isClarification' to the JSON key "is_clarification". Nullable Boolean is true if the LLM needs more details.
    
    @SerializedName("error") val error: String? = null
    // ^ Serializes 'error' to the JSON key "error". Nullable String holds error messages if the backend encounters a failure.
)
// ^ Ends QueryResponse class definition

data class TTSRequest(
// ^ data class representing the payload sent to the TTS service endpoint
    @SerializedName("text") val text: String,
    // ^ The text to be spoken
    
    @SerializedName("description") val description: String? = null
    // ^ Optional parameter providing a description of the speaker voice (used by Parler-TTS)
)
// ^ Ends TTSRequest class definition

data class TTSResponse(
// ^ data class representing the backend server response containing the generated audio
    @SerializedName("audio_base64") val audioBase64: String
    // ^ The base64-encoded audio WAV file string
)
// ^ Ends TTSResponse class definition

data class TranscriptionResponse(
// ^ data class representing the backend server response containing the transcribed text from STT
    @SerializedName("transcription") val transcription: String,
    // ^ The transcribed text from the audio
    
    @SerializedName("language") val language: String? = null,
    // ^ The detected language of the spoken text
    
    @SerializedName("processing_time") val processingTime: Double? = null
    // ^ The time taken by the server to transcribe the audio
)
// ^ Ends TranscriptionResponse class definition
