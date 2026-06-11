// File: android-app/app/src/main/java/com/rushdululilm/app/viewmodel/SettingsViewModel.kt
// Purpose: Manages the state of the Settings Screen
// Layer: 1 — Android UI Skeleton
// Depends on: ViewModel, Hilt, StateFlow
// Created: 2026-05-31 | Modified: 2026-06-10
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
import com.rushdululilm.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import com.rushdululilm.app.data.repository.UserPreferencesRepository
import com.rushdululilm.app.model.AppLanguage

/**
 * SettingsViewModel is the 'brain' for the Settings Screen.
 * It holds the user's choices (like which language or Madhab they prefer).
 * Analogy: Like a "Settings" page in a diary, it keeps track of your personal preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository // 💉 Injected to save settings centrally
) : ViewModel() {

    // --- SECTION 1: LANGUAGE SETTINGS ---
    // 'selectedLanguage' stores the shared language selected anywhere in the app.
    val selectedLanguage: StateFlow<AppLanguage> = preferencesRepository.selectedLanguage

    // --- SECTION 2: MADHAB PREFERENCE ---
    // We observe the preference directly from the central repository.
    val selectedMadhab: StateFlow<String> = preferencesRepository.selectedMadhab

    // Tracks the description of the selected Madhab to explain it to the user.
    private val _madhabDescription = MutableStateFlow(R.string.madhab_all_desc)
    val madhabDescription: StateFlow<Int> = _madhabDescription

    // --- SECTION 3: APP SETTINGS ---
    // 'isAutoPlayEnabled' is a true/false toggle for reading aloud automatically.
    private val _isAutoPlayEnabled = MutableStateFlow(false)
    val isAutoPlayEnabled: StateFlow<Boolean> = _isAutoPlayEnabled

    // 'isLargeTextEnabled' is a toggle for making text even larger for accessibility.
    private val _isLargeTextEnabled = MutableStateFlow(true)
    val isLargeTextEnabled: StateFlow<Boolean> = _isLargeTextEnabled

    // --- ACTIONS ---

    // Called when the user taps a different language.
    fun onLanguageSelected(language: AppLanguage) {
        preferencesRepository.updateLanguage(language)
    }

    /**
     * Called when the user taps a different Madhab/Source preference.
     * @param madhabKey The internal key ("all", "neutral", "hanafi")
     */
    fun onMadhabSelected(madhabKey: String) {
        // Save the choice in the central repository
        preferencesRepository.updateMadhab(madhabKey)
        
        // Update the explanation text based on the choice
        _madhabDescription.value = when (madhabKey) {
            "hanafi" -> R.string.madhab_hanafi_desc
            "neutral" -> R.string.madhab_neutral_desc
            else -> R.string.madhab_all_desc
        }
        
        println("Settings: Madhab changed to $madhabKey")
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
