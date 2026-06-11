// File: UserPreferencesRepository.kt
// Purpose: A central storage for user-selected settings that need to be shared across multiple screens.
// Layer: 4 — Connect Android to Backend (Repository)
// Depends on: AppLanguage.kt, AppCompatDelegate, StateFlow
// Created: 2026-06-09 | Modified: 2026-06-10
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.rushdululilm.app.model.AppLanguage
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

    // The current Android app locale stored by AppCompat, if one already exists.
    private val currentLocaleTag: String? = AppCompatDelegate
        .getApplicationLocales()
        .get(0)
        ?.toLanguageTag()

    // The currently selected app language shared by Home and Settings.
    private val _selectedLanguage = MutableStateFlow(AppLanguage.fromLanguageTag(currentLocaleTag))

    // Read-only language stream so ViewModels can observe the same current language.
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

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
     * Updates the app language in one place.
     *
     * @param language The supported language chosen by the user.
     * @return Unit because the repository updates shared state directly.
     */
    fun updateLanguage(language: AppLanguage) {
        _selectedLanguage.value = language

        val appLocale = LocaleListCompat.forLanguageTags(language.languageTag)

        AppCompatDelegate.setApplicationLocales(appLocale)

        println("Language changed to ${language.key} (${language.languageTag})")
    }

    /**
     * Updates the app language using a legacy string key.
     *
     * @param languageKey The old app key such as "Telugu", "Urdu", "Hindi", or "English".
     * @return Unit because the repository updates shared state directly.
     */
    fun updateLanguageByKey(languageKey: String) {
        updateLanguage(AppLanguage.fromKey(languageKey))
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
