// File: HomeViewModel.kt
// Purpose: The "brain" of the Home Screen. It remembers choices (like language) and handles actions (like tapping the mic).
// Layer: Layer 1 — Android App (ViewModel)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Represents the different phases the Home screen can be in.
 * A sealed class is like an enum, but it can hold data inside its states.
 */
sealed class HomeUiState {
    object Idle : HomeUiState() // Waiting for the user to press the mic
    object Recording : HomeUiState() // Currently capturing audio
    object Processing : HomeUiState() // Audio captured, waiting for server/AI response
    data class NavigatingToAnswer(val answerId: String) : HomeUiState() // Answer is ready, go to next screen
    data class Error(val message: String) : HomeUiState() // Something went wrong (e.g., no internet and no offline DB)
}

/**
 * The ViewModel for the Home Screen.
 * 
 * @HiltViewModel tells Hilt to construct this class for us automatically.
 * @Inject constructor() tells Hilt how to create it (even if empty).
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    // --- StateFlow Properties ---
    // StateFlow is like a live variable. When its value changes, the UI automatically redraws.
    // We use a private MutableStateFlow (can be changed) and a public StateFlow (read-only for the UI).

    // Tracks the currently selected language
    private val _selectedLanguage = MutableStateFlow("Telugu")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Tracks the currently selected Islamic source database
    private val _selectedSource = MutableStateFlow("all")
    val selectedSource: StateFlow<String> = _selectedSource.asStateFlow()

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
        // For now, this is a placeholder that just toggles the recording state visually.
        // In Phase 4, this will actually start/stop the Android AudioRecord API.
        _isRecording.value = !_isRecording.value
        
        if (_isRecording.value) {
            _uiState.value = HomeUiState.Recording
            println("Mic pressed: Started recording...") // Placeholder log
        } else {
            _uiState.value = HomeUiState.Idle
            println("Mic pressed: Stopped recording.") // Placeholder log
        }
    }

    /**
     * Called when the user selects a new language from the dropdown.
     */
    fun onLanguageSelected(language: String) {
        _selectedLanguage.value = language
        println("Language changed to: $language") // Placeholder log
    }

    /**
     * Called when the user taps a different source chip.
     */
    fun onSourceSelected(source: String) {
        _selectedSource.value = source
        println("Source changed to: $source") // Placeholder log
    }
}
