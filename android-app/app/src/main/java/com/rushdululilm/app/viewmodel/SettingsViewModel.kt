// File: SettingsViewModel.kt
// Purpose: Manages the state of the Settings Screen
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: UserPreferencesRepository.kt, AppLanguage.kt, StateFlow
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
// ^ Base Android Architecture Component class designed to store and manage UI-related data in a lifecycle-aware way
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry to access string resource IDs
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Annotation that registers this ViewModel for constructor injection with Hilt
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Flow class that holds a single state value and supports write access
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Flow class that holds a single state value and supports read-only access
import javax.inject.Inject
// ^ Tells Hilt how to inject constructor dependencies
import com.rushdululilm.app.data.repository.UserPreferencesRepository
// ^ Imports the singleton repository managing user preferences
import com.rushdululilm.app.model.AppLanguage
// ^ Imports the central supported language Enum definition

// 🏛️ CONCEPT: SettingsViewModel coordinates preferences state for the Settings screen.
//    It observes global variables (locale, Madhab) from the UserPreferencesRepository and updates them, while maintaining local-only settings like text size.
// 🏛️ ANALOGY: SettingsViewModel is like the climate control board on a car dashboard. 
//    Toggling a switch (like fan speed or temperature) updates the dials on the screen and changes the settings in the engine.
@HiltViewModel
// ^ Instructs Hilt dependency injection to manage construction of this ViewModel class
class SettingsViewModel @Inject constructor( // ^ class SettingsViewModel manages settings state, constructor injected by Hilt
    private val preferencesRepository: UserPreferencesRepository // ^ central user preferences repository reference
) : ViewModel() {
// ^ SettingsViewModel inherits from base architecture ViewModel class

    val selectedLanguage: StateFlow<AppLanguage> = preferencesRepository.selectedLanguage
    // ^ Exposes a read-only StateFlow linked to the global app language preference

    val selectedMadhab: StateFlow<String> = preferencesRepository.selectedMadhab
    // ^ Exposes a read-only StateFlow linked to the global Madhab choice string

    private val _madhabDescription = MutableStateFlow(R.string.madhab_all_desc)
    // ^ private mutable flow holding the string resource ID of the explanation text for the selected Madhab (defaults to All description)
    
    val madhabDescription: StateFlow<Int> = _madhabDescription
    // ^ public read-only StateFlow holding the active description string resource ID

    private val _isAutoPlayEnabled = MutableStateFlow(false)
    // ^ private mutable flow tracking whether text-to-speech auto-play is enabled (starts as false)
    
    val isAutoPlayEnabled: StateFlow<Boolean> = _isAutoPlayEnabled
    // ^ public read-only StateFlow exposed to the UI for auto-play toggle status

    private val _isLargeTextEnabled = MutableStateFlow(true)
    // ^ private mutable flow tracking whether large text mode is enabled (starts as true for accessibility)
    
    val isLargeTextEnabled: StateFlow<Boolean> = _isLargeTextEnabled
    // ^ public read-only StateFlow exposed to the UI for large text toggle status

    fun onLanguageSelected(language: AppLanguage) {
    // ^ Triggered when the user selects a language option in the Settings UI list
        preferencesRepository.updateLanguage(language)
        // ^ Updates the central preference repository, triggering runtime language change
    }
    // ^ Ends onLanguageSelected function

    fun onMadhabSelected(madhabKey: String) {
    // ^ Triggered when the user clicks on a Madhab preference option
        preferencesRepository.updateMadhab(madhabKey)
        // ^ Updates the central repository with the new Madhab key selection
        
        _madhabDescription.value = when (madhabKey) {
        // ^ Conditional block updating the explanation text description resource based on the key choice
            "hanafi" -> R.string.madhab_hanafi_desc
            // ^ Sets description to Hanafi definition if selected
            "neutral" -> R.string.madhab_neutral_desc
            // ^ Sets description to Neutral definition if selected
            else -> R.string.madhab_all_desc
            // ^ Defaults to All definition
        }
        // ^ Ends conditional check
        
        println("Settings: Madhab changed to $madhabKey")
        // ^ Prints a debug statement to the console
    }
    // ^ Ends onMadhabSelected function

    fun onAutoPlayToggled(enabled: Boolean) {
    // ^ Triggered when the user toggles the auto-play switch
        _isAutoPlayEnabled.value = enabled
        // ^ Updates the local state flow value
        println("Settings: Auto-play toggled to $enabled")
        // ^ Prints a debug log statement
    }
    // ^ Ends onAutoPlayToggled function

    fun onLargeTextToggled(enabled: Boolean) {
    // ^ Triggered when the user toggles the large text accessibility switch
        _isLargeTextEnabled.value = enabled
        // ^ Updates the local state flow value
        println("Settings: Large text toggled to $enabled")
        // ^ Prints a debug log statement
    }
    // ^ Ends onLargeTextToggled function
}
// ^ Ends SettingsViewModel class definition
