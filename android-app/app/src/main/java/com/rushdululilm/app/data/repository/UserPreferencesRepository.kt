// File: UserPreferencesRepository.kt
// Purpose: A central storage for user-selected settings that need to be shared across multiple screens.
// Layer: Layer 1 — Android App (Data Repository)
// Depends on: AppLanguage.kt, AppCompatDelegate, StateFlow
// Created: 2026-06-09 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.data.repository

import androidx.appcompat.app.AppCompatDelegate
// ^ Android library class used to apply global application behavior (like changing language/theme)
import androidx.core.os.LocaleListCompat
// ^ Android helper class that manages lists of language preferences (locales) in a backward-compatible way
import com.rushdululilm.app.model.AppLanguage
// ^ Imports our app's central Enum definition for supported languages
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Coroutines flow class that holds a single value and lets us change it (write access)
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Coroutines flow class that holds a single value and lets others read and watch it (read-only access)
import kotlinx.coroutines.flow.asStateFlow
// ^ Kotlin extension function to convert a mutable flow into a read-only StateFlow for safety
import javax.inject.Inject
// ^ Standard Java injection annotation used to ask Hilt to construct this object when needed
import javax.inject.Singleton
// ^ Standard Java annotation telling Hilt to create only one instance of this repository for the entire app lifetime

// 🏛️ CONCEPT: A Repository handles data operations. In this class, we store local user choices.
//    MutableStateFlow is used to write changes internally, and StateFlow is exposed read-only so ViewModels can watch for updates.
//    AppCompatDelegate.setApplicationLocales() changes the language at runtime, rebuilding the activity automatically.
// 🏛️ ANALOGY: UserPreferencesRepository is like a central settings blackboard in a workshop. 
//    When a worker changes a choice (like changing tools), they write it on the blackboard (MutableStateFlow) so everyone else (ViewModels) instantly sees it (StateFlow).
@Singleton
// ^ Tells Hilt to preserve this repository instance globally so preferences are shared app-wide
class UserPreferencesRepository @Inject constructor() {
// ^ constructor injected with Hilt. Hilt will automatically construct it when requested.

    private val currentLocaleTag: String? = AppCompatDelegate
    // ^ private read-only variable holding the current active locale tag string
        .getApplicationLocales()
        // ^ Gets the list of active languages from the Android application delegate
        .get(0)
        // ^ Retrieves the primary language configuration (first item in the list)
        ?.toLanguageTag()
        // ^ Converts the locale object to a standard string (like "te" or "ur")

    private val _selectedLanguage = MutableStateFlow(AppLanguage.fromLanguageTag(currentLocaleTag))
    // ^ private read-only variable holding a MutableStateFlow containing the AppLanguage matching the current locale tag

    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()
    // ^ public read-only StateFlow exposed to ViewModels so they can observe language selection updates

    private val _selectedMadhab = MutableStateFlow("all")
    // ^ private read-only variable holding a MutableStateFlow representing the active Madhab setting (defaults to "all")
    
    val selectedMadhab: StateFlow<String> = _selectedMadhab.asStateFlow()
    // ^ public read-only StateFlow exposed to ViewModels to observe Madhab settings updates

    fun updateMadhab(madhabKey: String) {
    // ^ Function to write a new Madhab key preference to the repository state
        _selectedMadhab.value = madhabKey
        // ^ Updates the MutableStateFlow value, which instantly triggers updates to all observers
    }
    // ^ Ends updateMadhab function

    fun updateLanguage(language: AppLanguage) {
    // ^ Function to update the active app language and notify components
        _selectedLanguage.value = language
        // ^ Updates the internal MutableStateFlow value to the new language selection

        val appLocale = LocaleListCompat.forLanguageTags(language.languageTag)
        // ^ Constructs a Locale list compat wrapper containing the target language ISO code (like "te")

        AppCompatDelegate.setApplicationLocales(appLocale)
        // ^ Tells Android to set the active app language. This will recreate screens using the new resources.

        println("Language changed to ${language.key} (${language.languageTag})")
        // ^ Prints a debug log console message showing the language update
    }
    // ^ Ends updateLanguage function

    fun updateLanguageByKey(languageKey: String) {
    // ^ Helper function to update app language using a raw string key
        updateLanguage(AppLanguage.fromKey(languageKey))
        // ^ Looks up the AppLanguage enum using the string key and updates the configuration
    }
    // ^ Ends updateLanguageByKey function
    
    fun getBackendSources(): List<String> {
    // ^ Helper function that translates the UI Madhab selection key into backend database collection name filters
        return when (_selectedMadhab.value) {
        // ^ Evaluates the active Madhab state key
            "hanafi" -> listOf("deoband") 
            // ^ Returns "deoband" collection filter if Hanafi is selected
            "neutral" -> listOf("islamqa") 
            // ^ Returns "islamqa" collection filter if Neutral is selected
            else -> listOf("islamqa", "deoband") 
            // ^ Returns both collections if "all" or any other value is selected
        }
        // ^ Ends conditional check
    }
    // ^ Ends getBackendSources function
}
// ^ Ends UserPreferencesRepository class definition
