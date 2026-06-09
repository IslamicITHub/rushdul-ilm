// File: MainRepository.kt
// Purpose: The central data manager that coordinates between network (Retrofit) and local storage.
// Layer: 4 — Connect Android to Backend
// Created: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.repository

import com.rushdululilm.app.data.remote.ApiService
import com.rushdululilm.app.data.remote.QueryRequest
import com.rushdululilm.app.data.remote.QueryResponse
import com.rushdululilm.app.utils.Resource
import com.rushdululilm.app.model.FatwaAnswer
import com.rushdululilm.app.model.FatwaSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// 🏛️ The Repository is the "Single Source of Truth" for data in the app.
// @Singleton means Hilt will create only one copy of this for the whole app.
@Singleton
class MainRepository @Inject constructor(
    private val apiService: ApiService // Hilt provides the ApiService we created earlier
) {

    // Holds the latest answer fetched from the backend.
    private val _latestAnswer = MutableStateFlow<FatwaAnswer?>(null)
    val latestAnswer: StateFlow<FatwaAnswer?> = _latestAnswer.asStateFlow()

    // 🤖 This function sends a question to our AI backend.
    // It returns a 'Flow' or just a direct 'Resource' depending on preference.
    // For now, we use a simple suspend function.
    suspend fun askQuestion(request: QueryRequest): Resource<QueryResponse> {
        return try {
            // 1. Tell the caller we are starting to load
            // (The caller will handle the 'Loading' state if needed)
            
            // 2. Make the actual network call
            val response = apiService.askQuestion(request)
            
            // 3. Check if the server responded with a 200 OK
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                
                // Check if the server returned a Python-level error
                if (responseBody.error != null) {
                    return Resource.Error(responseBody.error)
                }
                
                // Update the shared latestAnswer flow
                // We map all source URLs from the backend to our FatwaSource list
                val fatwaSources = responseBody.sources?.map { url ->
                    val name = if (url.contains("islamqa.info")) "IslamQA.info" 
                               else if (url.contains("deoband")) "Darul Ifta Deoband" 
                               else "Islamic Source"
                    FatwaSource(name, url)
                } ?: emptyList()
                                 
                _latestAnswer.value = FatwaAnswer(
                    id = System.currentTimeMillis().toString(),
                    questionText = responseBody.question ?: request.question,
                    answerText = responseBody.answer ?: "No answer received",
                    sources = fatwaSources,
                    language = "en", // We'll detect/set language properly later
                    isOfflineCache = false,
                    expandedQuery = responseBody.expandedSearchQuery
                )
                
                // Return the successful answer
                Resource.Success(responseBody)
            } else {
                // Return the server's error message
                Resource.Error(response.message() ?: "Unknown Server Error")
            }
        } catch (e: Exception) {
            // 4. Handle crashes (like no internet)
            Resource.Error(e.localizedMessage ?: "Network connection failed")
        }
    }

    // 🏥 Check if the server is healthy
    suspend fun checkServerHealth(): Resource<Boolean> {
        return try {
            val response = apiService.checkHealth()
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Server unhealthy")
            }
        } catch (e: Exception) {
            Resource.Error("Could not reach server")
        }
    }
}
