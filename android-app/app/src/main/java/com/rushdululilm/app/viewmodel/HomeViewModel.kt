// File: HomeViewModel.kt
// Purpose: The "brain" of the Home Screen. It remembers choices (like language) and handles actions (like tapping the mic).
// Layer: Layer 1 — Android App (ViewModel)
// Created: 2026-05-31 | Modified: 2026-06-08 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rushdululilm.app.data.remote.QueryRequest
import com.rushdululilm.app.data.repository.MainRepository
import com.rushdululilm.app.data.repository.UserPreferencesRepository
import com.rushdululilm.app.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the different phases the Home screen can be in.
 * A sealed class is like an enum, but it can hold data inside its states.
 */
sealed class HomeUiState {
    object Idle : HomeUiState() // Waiting for the user to press the mic
    object Recording : HomeUiState() // Currently capturing audio
    object Processing : HomeUiState() // Audio captured, waiting for server/AI response
    // 📝 Answer is ready, go to next screen.
    object NavigatingToAnswer : HomeUiState() 
    data class Error(val message: String) : HomeUiState() // Something went wrong (e.g., no internet and no offline DB)
}

/**
 * The ViewModel for the Home Screen.
 * 
 * @HiltViewModel tells Hilt to construct this class for us automatically.
 * @Inject constructor() tells Hilt how to create it.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository, // 💉 Hilt gives us the Repository automatically
    private val preferencesRepository: UserPreferencesRepository // 💉 Central source for user choices
) : ViewModel() {

    // --- StateFlow Properties ---
    // StateFlow is like a live variable. When its value changes, the UI automatically redraws.
    // We use a private MutableStateFlow (can be changed) and a public StateFlow (read-only for the UI).

    // Tracks the currently selected language
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Tracks the currently selected Islamic source database
    // We observe this from the central preferences repository
    val selectedSource: StateFlow<String> = preferencesRepository.selectedMadhab

    // Tracks whether the microphone is currently active
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Tracks the overall phase of the screen
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // --- User Actions ---

    /**
     * Called when the user taps the large microphone button.
     */
    fun onMicPressed() {
        // Toggle the recording state visually.
        _isRecording.value = !_isRecording.value
        
        if (_isRecording.value) {
            _uiState.value = HomeUiState.Recording
            println("Mic pressed: Started recording...") 
        } else {
            // 🧪 TEST LOGIC: If we just finished "recording", send a test query to the server.
            sendTestQuery()
        }
    }

    /**
     * Temporary function to test the connection to the FastAPI backend.
     */
    private fun sendTestQuery() {
        // Run this in the background so the UI doesn't freeze
        viewModelScope.launch {
            _uiState.value = HomeUiState.Processing
            
            // 🔍 We get the actual list of database names based on user settings
            // For example, if "Hanafi" is picked, this returns ["deoband"]
            val sourcesToSearch = preferencesRepository.getBackendSources()
            
            // Create a request with a hardcoded test question
            val request = QueryRequest(
                question = "How many sunna prayers are there in total on Friday at afternoon?",
                sources = sourcesToSearch
            )
            
            // Call the repository to get the answer
            val result = repository.askQuestion(request)
            
            when (result) {
                is Resource.Success -> {
                    // 🎉 Success! The repository now holds the latestAnswer.
                    // Transition to NavigatingToAnswer so the UI can navigate.
                    _uiState.value = HomeUiState.NavigatingToAnswer
                    println("✅ API Success, navigating...")
                }
                is Resource.Error -> {
                    // ❌ Error! Show the error message to the user.
                    _uiState.value = HomeUiState.Error(result.message ?: "Unknown Error")
                    println("❌ API Error: ${result.message}")
                }
                is Resource.Loading -> {
                    _uiState.value = HomeUiState.Processing
                }
            }
        }
    }

    /**
     * Resets the UI state back to Idle.
     * Call this after navigating to the AnswerScreen.
     */
    fun resetUiState() {
        _uiState.value = HomeUiState.Idle
    }

    /**
     * Called when the user selects a new language from the dropdown.
     */
    /*
    fun onLanguageSelected(language: String) {
        _selectedLanguage.value = language
        println("Language changed to: $language") 
    }
    */
    fun onLanguageSelected(language: String) {
        _selectedLanguage.value = language

        // 1. Map the display name to the ISO language code
        val languageCode = when (language) {
            "Telugu" -> "te"
            "Urdu" -> "ur"
            "Hindi" -> "hi"
            "English" -> "en"
            else -> "en"
        }

        // 2. Tell Android to change the app's language
        // This will cause the UI to redraw using the correct strings.xml file
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        println("Settings: Language changed to $language ($languageCode)")
    }

    /**
     * Called when the user taps a different source chip.
     */
    fun onSourceSelected(source: String) {
        // Update the central preference repository
        preferencesRepository.updateMadhab(source)
        println("Source changed to: $source") 
    }
}
