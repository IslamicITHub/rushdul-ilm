// File: android-app/app/src/main/java/com/rushdululilm/app/viewmodel/SettingsViewModel.kt
// Purpose: Manages the state of the Settings Screen
// Layer: 1 — Android UI Skeleton
// Depends on: ViewModel, Hilt, StateFlow
// Created: 2026-05-31 | Modified: 2026-05-31
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * SettingsViewModel is the 'brain' for the Settings Screen.
 * It holds the user's choices (like which language or Madhab they prefer).
 * Analogy: Like a "Settings" page in a diary, it keeps track of your personal preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    // --- SECTION 1: LANGUAGE SETTINGS ---
    // 'selectedLanguage' stores which language the user has picked.
    // Default is "Telugu".
    private val _selectedLanguage = MutableStateFlow("Telugu")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    // --- SECTION 2: MADHAB PREFERENCE ---
    // 'selectedMadhab' stores the preferred Islamic source school.
    // Default is "All Sources".
    private val _selectedMadhab = MutableStateFlow("All Sources")
    val selectedMadhab: StateFlow<String> = _selectedMadhab

    // --- SECTION 3: APP SETTINGS ---
    // 'isAutoPlayEnabled' is a true/false toggle for reading aloud automatically.
    private val _isAutoPlayEnabled = MutableStateFlow(false)
    val isAutoPlayEnabled: StateFlow<Boolean> = _isAutoPlayEnabled

    // 'isLargeTextEnabled' is a toggle for making text even larger for accessibility.
    private val _isLargeTextEnabled = MutableStateFlow(true)
    val isLargeTextEnabled: StateFlow<Boolean> = _isLargeTextEnabled

    // --- ACTIONS ---

    // Called when the user taps a different language.
    fun onLanguageSelected(language: String) {
        _selectedLanguage.value = language
        println("Settings: Language changed to $language")
    }

    // Called when the user taps a different Madhab/Source preference.
    fun onMadhabSelected(madhab: String) {
        _selectedMadhab.value = madhab
        println("Settings: Madhab changed to $madhab")
    }

    // Called when the user toggles the Auto-play switch.
    fun onAutoPlayToggled(enabled: Boolean) {
        _isAutoPlayEnabled.value = enabled
        println("Settings: Auto-play toggled to $enabled")
    }

    // Called when the user toggles the Large Text switch.
    fun onLargeTextToggled(enabled: Boolean) {
        _isLargeTextEnabled.value = enabled
        println("Settings: Large text toggled to $enabled")
    }
}
