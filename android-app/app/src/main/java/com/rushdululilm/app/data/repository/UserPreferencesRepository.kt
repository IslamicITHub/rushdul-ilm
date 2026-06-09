// File: UserPreferencesRepository.kt
// Purpose: A central storage for user-selected settings that need to be shared across multiple screens.
// Layer: 4 — Connect Android to Backend (Repository)
// Created: 2026-06-09 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🏛️ UserPreferencesRepository stores personal choices made by the user in the Settings screen.
 * 
 * Analogy: Like a "Settings" file in a video game, this class remembers your preferences
 * so that they can be used whenever you perform an action (like asking a question).
 * 
 * @Singleton ensures there is only ONE copy of these settings in the entire app.
 */
@Singleton
class UserPreferencesRepository @Inject constructor() {

    // 🔒 Private 'MutableStateFlow' can be changed inside this class.
    // We default to "all" so the user gets all information by default.
    private val _selectedMadhab = MutableStateFlow("all")
    
    // 🌍 Public 'StateFlow' is read-only for ViewModels.
    // This allows other parts of the app to "watch" the preference for changes.
    val selectedMadhab: StateFlow<String> = _selectedMadhab.asStateFlow()

    /**
     * ✍️ Updates the stored Madhab preference.
     * @param madhabKey The unique key for the choice: "all", "neutral", or "hanafi".
     */
    fun updateMadhab(madhabKey: String) {
        // Set the new value, which will automatically notify anyone watching it.
        _selectedMadhab.value = madhabKey
    }
    
    /**
     * 🛠️ Helper function to convert the chosen preference into actual database collection names.
     * This maps our UI choices ("hanafi") to backend names ("deoband").
     */
    fun getBackendSources(): List<String> {
        return when (_selectedMadhab.value) {
            "hanafi" -> listOf("deoband") // Only search Deoband for Hanafi
            "neutral" -> listOf("islamqa") // Only search IslamQA for Neutral
            else -> listOf("islamqa", "deoband") // Search everything for "all"
        }
    }
}
