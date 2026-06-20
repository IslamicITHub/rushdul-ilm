// File: MainRepository.kt
// Purpose: The central data manager that coordinates between network (Retrofit) and local storage.
// Layer: Layer 1 — Android App (Data Repository)
// Depends on: ApiService.kt, NetworkModels.kt, Resource.kt, AnswerModels.kt
// Created: 2026-06-08 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.repository

import com.rushdululilm.app.data.remote.ApiService
// ^ Imports our ApiService interface detailing network endpoints
import com.rushdululilm.app.data.remote.QueryRequest
// ^ Imports the request payload model containing question and settings filters
import com.rushdululilm.app.data.remote.QueryResponse
// ^ Imports the response payload model containing RAG search results
import com.rushdululilm.app.data.remote.TTSRequest
// ^ Imports the request model for Text-To-Speech endpoint
import com.rushdululilm.app.data.remote.TTSResponse
// ^ Imports the response model for Text-To-Speech endpoint
import com.rushdululilm.app.utils.Resource
// ^ Imports the sealed class handling UI states (Success, Error, Loading)
import com.rushdululilm.app.model.FatwaAnswer
// ^ Imports the data class model representing the UI representation of a fatwa answer
import com.rushdululilm.app.model.FatwaSource
// ^ Imports the data class model representing individual fatwa source citations
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Coroutines flow class that holds a single value and lets us write changes to it
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Coroutines flow class that holds a single value and lets others read it (read-only)
import kotlinx.coroutines.flow.asStateFlow
// ^ Kotlin extension function to convert a mutable flow into a read-only StateFlow for safety
import javax.inject.Inject
// ^ Standard Java injection annotation used to ask Hilt to supply dependencies
import javax.inject.Singleton
// ^ Standard Java annotation telling Hilt to create only one instance of this repository

// 🏛️ CONCEPT: The Repository pattern abstracts data access from the presentation layer (ViewModels).
//    The ViewModel requests data, and the Repository decides whether to fetch it from the network or database.
//    MutableStateFlow maintains the latest fetched FatwaAnswer so it can be observed by different UI screens.
// 🏛️ ANALOGY: The Repository is like a librarian. When a student (ViewModel) wants a book (data), 
//    they ask the librarian. The librarian searches online databases (Retrofit) or offline bookshelves (Room DB) and hands it back.
@Singleton
// ^ Tells Hilt to share a single instance of MainRepository globally across the application
class MainRepository @Inject constructor( // ^ class MainRepository handles RAG server queries, constructor injected by Hilt
    private val apiService: ApiService, // ^ Retrofit-created ApiService interface client reference
    private val answerHistoryRepository: AnswerHistoryRepository
    // ^ Injected repository for saving retrieved answers to the local Room database
) {
// ^ Starts MainRepository body

    private val _latestAnswer = MutableStateFlow<FatwaAnswer?>(null)
    // ^ private read-only mutable flow that holds the latest successfully retrieved fatwa answer (starts as null)
    
    val latestAnswer: StateFlow<FatwaAnswer?> = _latestAnswer.asStateFlow()
    // ^ public read-only StateFlow exposed to ViewModels to observe the latest fatwa updates

    suspend fun askQuestion(request: QueryRequest): Resource<QueryResponse> {
    // ^ suspend function to send a user question to the backend. Runs asynchronously on a background thread.
        return try {
        // ^ try block to catch network crashes or timeout exceptions safely
            val response = apiService.askQuestion(request)
            // ^ Performs the HTTP POST network request via Retrofit and waits for the server response
            
            if (response.isSuccessful && response.body() != null) {
            // ^ Checks if the HTTP status is successful (e.g. 200 OK) and the response body is not null
                val responseBody = response.body()!!
                // ^ Force-unwraps the non-null response body object safely since we just verified it exists
                
                if (responseBody.error != null) {
                // ^ Checks if the backend server sent a Python-level error message inside the payload
                    return Resource.Error(responseBody.error)
                    // ^ Returns a Resource.Error status containing the server's error message
                }
                // ^ Ends error check
                
                val fatwaSources = responseBody.sources?.map { url ->
                // ^ Maps the list of source URL strings from the backend into a list of FatwaSource objects
                    val name = if (url.contains("islamqa.info")) "IslamQA.info" 
                    // ^ Assigns name "IslamQA.info" if the URL contains the site name
                               else if (url.contains("deoband")) "Darul Ifta Deoband" 
                               // ^ Assigns name "Darul Ifta Deoband" if the URL contains the Hanafi domain
                               else "Islamic Source"
                               // ^ Otherwise defaults to generic name "Islamic Source"
                    FatwaSource(name, url)
                    // ^ Instantiates and returns a new FatwaSource object with mapped name and URL
                } ?: emptyList()
                // ^ Defaults to an empty list if the sources array in response is null
                                 
                val newAnswer = FatwaAnswer(
                // ^ Constructs a new FatwaAnswer object to hold the retrieved data
                    id = System.currentTimeMillis().toString(),
                    // ^ Generates a unique string ID based on the current timestamp in milliseconds
                    questionText = responseBody.question ?: request.question,
                    // ^ Uses the server's processed question string, or falls back to the original request string
                    answerText = responseBody.answer ?: "No answer received",
                    // ^ Uses the returned answer text, or falls back to a default "No answer received" string
                    sources = fatwaSources,
                    // ^ Sets the list of parsed FatwaSource citations
                    language = "en", 
                    // ^ Placeholder string for language tracking
                    isOfflineCache = false,
                    // ^ Sets offline indicator to false (as this was fetched from the remote apiService)
                    expandedQuery = responseBody.expandedSearchQuery
                    // ^ Sets the technical expanded query returned by the RAG backend
                )
                // ^ Ends FatwaAnswer construction
                
                _latestAnswer.value = newAnswer
                // ^ Updates the MutableStateFlow state with the new mapped FatwaAnswer
                
                answerHistoryRepository.saveFatwaAnswer(newAnswer, newAnswer.language, newAnswer.isOfflineCache)
                // ^ Saves the retrieved answer automatically to the local database for offline history
                
                Resource.Success(responseBody)
                // ^ Returns a Resource.Success object containing the raw network response
            } else {
            // ^ Executes if the HTTP response failed (e.g. status 500 or 404)
                Resource.Error(response.message() ?: "Unknown Server Error")
                // ^ Returns a Resource.Error containing the HTTP status message or a default string
            }
            // ^ Ends response success check
        } catch (e: Exception) {
        // ^ Catches network sockets exceptions (like no internet access) or serialization failures
            Resource.Error(e.localizedMessage ?: "Network connection failed")
            // ^ Returns a Resource.Error wrapping the exception message
        }
        // ^ Ends try-catch block
    }
    // ^ Ends askQuestion function

    suspend fun checkServerHealth(): Resource<Boolean> {
    // ^ suspend function to check server connection status
        return try {
        // ^ try block to catch connectivity failures
            val response = apiService.checkHealth()
            // ^ Calls the GET health check endpoint on the backend
            if (response.isSuccessful) {
            // ^ Checks if the server responded with 200 OK
                Resource.Success(true)
                // ^ Returns success containing true (server online)
            } else {
            // ^ Executes if server responded with an error code
                Resource.Error("Server unhealthy")
                // ^ Returns error status
            }
            // ^ Ends response validation check
        } catch (e: Exception) {
        // ^ Catches connection timeout or host unreachable exceptions
            Resource.Error("Could not reach server")
            // ^ Returns error status indicating the host is offline
        }
        // ^ Ends try-catch block
    }
    // ^ Ends checkServerHealth function
    suspend fun generateSpeech(text: String, description: String = "A female speaker delivers a clear and neutral speech."): Resource<TTSResponse> {
    // ^ suspend function to request audio synthesis from the TTS backend service
        return try {
        // ^ try block to handle network exceptions safely
            val request = TTSRequest(text = text, description = description)
            // ^ Constructs the JSON payload object containing the text to be spoken
            
            val response = apiService.generateSpeech("http://192.168.0.102:8002/tts", request)
            // ^ Performs the HTTP POST request to the custom TTS port 8002
            
            if (response.isSuccessful && response.body() != null) {
            // ^ Checks if the HTTP status is successful (200 OK)
                Resource.Success(response.body()!!)
                // ^ Returns the base64 audio response string wrapped in a Success status
            } else {
            // ^ Executes if the server returned an error (e.g. 500)
                Resource.Error(response.message() ?: "Failed to generate speech")
                // ^ Returns an Error status with the HTTP message
            }
            // ^ Ends validation check
        } catch (e: Exception) {
        // ^ Catches network unreachable exceptions
            Resource.Error(e.localizedMessage ?: "Network connection failed")
            // ^ Returns an Error status with the exception details
        }
        // ^ Ends try-catch block
    }
    // ^ Ends generateSpeech function
}
// ^ Ends MainRepository class definition
