// File: NetworkModels.kt
// Purpose: Defines the JSON data structures for communication with the FastAPI backend.
// Layer: 4 — Connect Android to Backend
// Created: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.remote

import com.google.gson.annotations.SerializedName

// 📦 This class represents the question we send to the server.
// It matches the 'QueryRequest' model in our FastAPI server.
data class QueryRequest(
    // @SerializedName ensures the name matches the JSON key exactly.
    @SerializedName("question") val question: String,
    
    // chat_history is a list of previous messages to keep the conversation context.
    @SerializedName("chat_history") val chatHistory: List<ChatMessage> = emptyList(),
    
    // sources is an optional list of which databases to search (e.g., ["islamqa", "deoband"]).
    @SerializedName("sources") val sources: List<String>? = null
)

// 📦 This represents a single message in the chat history.
data class ChatMessage(
    @SerializedName("role") val role: String, // "user" or "assistant"
    @SerializedName("content") val content: String // The actual text of the message
)

// 📦 This represents the answer we get back from the server.
data class QueryResponse(
    // The main text of the Islamic answer. 
    // Nullable because it might be missing if there's an error.
    @SerializedName("answer") val answer: String? = null,

    // The original question received by the server.
    @SerializedName("question") val question: String? = null,

    // The expanded version of the question used for database search.
    @SerializedName("expanded_search_query") val expandedSearchQuery: String? = null,
    
    // A list of clickable URLs for the sources used.
    @SerializedName("sources") val sources: List<String>? = null,
    
    // Whether the AI is asking for more details (clarification logic).
    @SerializedName("is_clarification") val isClarification: Boolean? = false,
    
    // ⚠️ New: If the server has a Python error, it might send this instead of an answer.
    @SerializedName("error") val error: String? = null
)
