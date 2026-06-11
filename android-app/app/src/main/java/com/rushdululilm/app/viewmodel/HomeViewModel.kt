// File: HomeViewModel.kt
// Purpose: The "brain" of the Home Screen. It remembers choices (like language) and handles actions (like tapping the mic).
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: MainRepository.kt, UserPreferencesRepository.kt, AppLanguage.kt, Resource.kt, NetworkModels.kt
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
// ^ Base Android Architecture Component class designed to store and manage UI-related data in a lifecycle-aware way
import androidx.lifecycle.viewModelScope
// ^ CoroutineScope tied to this ViewModel's lifecycle, automatically cancelled when the ViewModel is destroyed
import com.rushdululilm.app.data.remote.QueryRequest
// ^ Imports the HTTP POST request payload model
import com.rushdululilm.app.data.repository.MainRepository
// ^ Imports our central data coordinator
import com.rushdululilm.app.data.repository.UserPreferencesRepository
// ^ Imports the singleton repository managing user preferences
import com.rushdululilm.app.model.AppLanguage
// ^ Imports the central supported language Enum definition
import com.rushdululilm.app.utils.Resource
// ^ Imports the wrapper class managing UI network states
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Annotation that registers this ViewModel for constructor injection with Hilt
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Flow class that holds a single read-only state value
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Flow class that holds a single state value and supports write access
import kotlinx.coroutines.flow.asStateFlow
// ^ Extension function to convert a mutable flow into a read-only StateFlow
import kotlinx.coroutines.launch
// ^ Launches a concurrent background coroutine job within a CoroutineScope
import javax.inject.Inject
// ^ Annotation telling Hilt which constructor to use for creating this class

// 🏛️ CONCEPT: HomeUiState is a sealed class modeling the mutually exclusive phases of the Home Screen.
//    Sealed classes allow us to define closed hierarchies where each state is a distinct type, some holding custom parameters (like Error holding a message).
// 🏛️ ANALOGY: HomeUiState is like the gears of a car's automatic transmission. 
//    The car can only be in one gear at a time: "Park" (Idle), "Drive" (Processing), or "Reverse" (Error containing the crash reason).
sealed class HomeUiState {
// ^ Sealed class restricting subclasses to this file only
    object Idle : HomeUiState() 
    // ^ State representing the app waiting quietly for the user to press the mic button
    object Recording : HomeUiState() 
    // ^ State representing the microphone actively capturing user audio speech
    object Processing : HomeUiState() 
    // ^ State representing the app sending the recognized speech to the server and waiting for results
    object NavigatingToAnswer : HomeUiState() 
    // ^ State representing that the answer has arrived and the UI should transition to the Answer Screen
    data class Error(val message: String) : HomeUiState() 
    // ^ State representing a failure, carrying a custom error message string to be displayed on the screen
}
// ^ Ends HomeUiState sealed class definition

// 🏛️ CONCEPT: HomeViewModel binds the Home UI components to repositories. 
//    It registers preference StateFlows from the UserPreferencesRepository and maps mic clicks to network queries.
// 🏛️ ANALOGY: The ViewModel is like a chef in a restaurant kitchen. 
//    When the customer (UI) rings a bell (clicks the mic), the chef gets to work, gathers ingredients from the pantry (Repository), and returns the cooked meal (UI State).
@HiltViewModel
// ^ Instructs Hilt dependency injection to manage construction of this ViewModel class
class HomeViewModel @Inject constructor( // ^ class HomeViewModel manages state, constructor injected by Hilt
    private val repository: MainRepository, // ^ repository to perform database/network operations
    private val preferencesRepository: UserPreferencesRepository // ^ repository to retrieve user settings
) : ViewModel() {
// ^ HomeViewModel inherits from base architecture ViewModel class

    val selectedLanguage: StateFlow<AppLanguage> = preferencesRepository.selectedLanguage
    // ^ Exposes a read-only StateFlow linked to the global app language preference

    val selectedSource: StateFlow<String> = preferencesRepository.selectedMadhab
    // ^ Exposes a read-only StateFlow linked to the global Madhab filter choice

    private val _isRecording = MutableStateFlow(false)
    // ^ private mutable flow tracking whether the mic is active (starts as false)
    
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    // ^ public read-only StateFlow exposed to the Home Screen UI for styling the mic button pulse effect

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    // ^ private mutable state flow holding the current UI state (starts at Idle)
    
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    // ^ public read-only StateFlow exposed to the UI to control loaders and navigation triggers

    fun onMicPressed() {
    // ^ Triggered when the user taps the mic button
        _isRecording.value = !_isRecording.value
        // ^ Toggles the mic status boolean value
        
        if (_isRecording.value) {
        // ^ Checks if recording started
            _uiState.value = HomeUiState.Recording
            // ^ Sets screen state to Recording
            println("Mic pressed: Started recording...") 
            // ^ Prints a debug statement to the console
        } else {
        // ^ Executes when recording stops
            sendTestQuery()
            // ^ Triggers a test RAG query to backend (simulating audio query upload)
        }
        // ^ Ends recording toggle check
    }
    // ^ Ends onMicPressed function

    private fun sendTestQuery() {
    // ^ Simulates sending a speech-to-text string to the backend /query endpoint
        viewModelScope.launch {
        // ^ Launches a coroutine job bound to this ViewModel's lifecycle scope
            _uiState.value = HomeUiState.Processing
            // ^ Updates the UI state to Processing (showing loading spinner)
            
            val sourcesToSearch = preferencesRepository.getBackendSources()
            // ^ Queries user preferences to get database collection filter strings (e.g. ["deoband"])
            
            val request = QueryRequest(
            // ^ Constructs a QueryRequest object
                question = "How many sunna prayers are there in total on Friday at afternoon?",
                // ^ Hardcoded question string for integration testing
                sources = sourcesToSearch
                // ^ Mapped list of database collection filters
            )
            // ^ Ends QueryRequest construction
            
            val result = repository.askQuestion(request)
            // ^ Sends the request through MainRepository and waits for the network response
            
            when (result) {
            // ^ Evaluates the success/error wrapper of the result
                is Resource.Success -> {
                // ^ Runs if the response was successful
                    _uiState.value = HomeUiState.NavigatingToAnswer
                    // ^ Sets state to NavigatingToAnswer, triggering the UI navigation action
                    println("✅ API Success, navigating...")
                    // ^ Prints success debug log
                }
                // ^ Ends Success case match
                is Resource.Error -> {
                // ^ Runs if the response failed
                    _uiState.value = HomeUiState.Error(result.message ?: "Unknown Error")
                    // ^ Sets state to Error containing the error message to display in the UI
                    println("❌ API Error: ${result.message}")
                    // ^ Prints error debug log
                }
                // ^ Ends Error case match
                is Resource.Loading -> {
                // ^ Runs if loading status is returned
                    _uiState.value = HomeUiState.Processing
                    // ^ Sets state to Processing
                }
                // ^ Ends Loading case match
            }
            // ^ Ends when block
        }
        // ^ Ends coroutine block
    }
    // ^ Ends sendTestQuery function

    fun resetUiState() {
    // ^ Reset function called by screens immediately after performing navigation actions
        _uiState.value = HomeUiState.Idle
        // ^ Resets the state back to Idle to prevent navigation loops
    }
    // ^ Ends resetUiState function

    fun onLanguageSelected(language: AppLanguage) {
    // ^ Triggered when the user picks a language item from the dropdown selector
        preferencesRepository.updateLanguage(language)
        // ^ Updates the central repository, which automatically triggers locale switching
    }
    // ^ Ends onLanguageSelected function

    fun onSourceSelected(source: String) {
    // ^ Triggered when the user taps on a source preference chip
        preferencesRepository.updateMadhab(source)
        // ^ Updates the central preferences repository
        println("Source changed to: $source") 
        // ^ Prints a debug message in the console
    }
    // ^ Ends onSourceSelected function
}
// ^ Ends HomeViewModel class definition
