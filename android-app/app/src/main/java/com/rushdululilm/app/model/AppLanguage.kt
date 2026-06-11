// File: AppLanguage.kt
// Purpose: Defines every app language in one modular place so language choices stay consistent across screens.
// Layer: Layer 1 — Android App (Model)
// Depends on: R.string language labels
// Created: 2026-06-10 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.model

import androidx.annotation.StringRes
// ^ Android annotation indicating that an integer parameter or property represents a string resource ID from strings.xml
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry to access strings, layout dimensions, and assets

// 🏛️ CONCEPT: An enum class defines a fixed list of constants. Each constant (language) is an object
//    with its own properties: a stable database key, a standard Android locale tag, and a display name resource.
// 🏛️ ANALOGY: AppLanguage is like a restaurant menu listing the items we offer. 
//    Instead of letting waiters write any language name on a ticket (spelling errors), they select from this official menu.
enum class AppLanguage(
// ^ Declares an enum class named AppLanguage
    val key: String,
    // ^ Stable internal key (e.g. "Telugu") used inside database tables and settings repository
    val languageTag: String,
    // ^ Standard system ISO locale tag (e.g. "te") used by the Android operating system to load localized values
    @StringRes val displayNameRes: Int
    // ^ Integer ID matching a bilingual string label in strings.xml (e.g. R.string.language_telugu_display)
) {
// ^ Starts AppLanguage definition
    TELUGU(
    // ^ Enum constant representing the Telugu language option
        key = "Telugu",
        // ^ Maps key value to "Telugu"
        languageTag = "te",
        // ^ Maps ISO tag value to "te"
        displayNameRes = R.string.language_telugu_display
        // ^ Maps displayNameRes resource reference to the Telugu label in strings.xml
    ),
    // ^ Ends TELUGU declaration
    URDU(
    // ^ Enum constant representing the Urdu language option
        key = "Urdu",
        // ^ Maps key value to "Urdu"
        languageTag = "ur",
        // ^ Maps ISO tag value to "ur"
        displayNameRes = R.string.language_urdu_display
        // ^ Maps displayNameRes resource reference to the Urdu label in strings.xml
    ),
    // ^ Ends URDU declaration
    HINDI(
    // ^ Enum constant representing the Hindi language option
        key = "Hindi",
        // ^ Maps key value to "Hindi"
        languageTag = "hi",
        // ^ Maps ISO tag value to "hi"
        displayNameRes = R.string.language_hindi_display
        // ^ Maps displayNameRes resource reference to the Hindi label in strings.xml
    ),
    // ^ Ends HINDI declaration
    ENGLISH(
    // ^ Enum constant representing the English language option
        key = "English",
        // ^ Maps key value to "English"
        languageTag = "en",
        // ^ Maps ISO tag value to "en"
        displayNameRes = R.string.language_english_display
        // ^ Maps displayNameRes resource reference to the English label in strings.xml
    );
    // ^ Ends ENGLISH declaration and constants list

    companion object {
    // ^ companion object hosts static-like methods and variables that can be accessed directly using the class name
        val default: AppLanguage = TELUGU
        // ^ Defines the default app language as Telugu (since the app serves Kurnool users primarily)

        fun fromKey(key: String): AppLanguage {
        // ^ Helper function to convert an internal key string back into an AppLanguage enum object
            return entries.firstOrNull { language ->
            // ^ Searches through the list of enum constants (entries)
                language.key == key
                // ^ Matches the language key with the search parameter
            } ?: default
            // ^ If no match is found, falls back to the default language (Telugu)
        }
        // ^ Ends fromKey function

        fun fromLanguageTag(languageTag: String?): AppLanguage {
        // ^ Helper function to convert system-provided locale tags into our AppLanguage configuration
            val normalizedLanguageTag = languageTag
            // ^ Takes the input languageTag string
                ?.substringBefore("-")
                // ^ Normalizes tags by removing sub-tags (e.g. "te-IN" becomes "te")
                ?.substringBefore("_")
                // ^ Normalizes tags by removing alternate separators (e.g. "te_IN" becomes "te")

            return entries.firstOrNull { language ->
            // ^ Searches through the list of enum constants
                language.languageTag == normalizedLanguageTag
                // ^ Matches the normalized tag with the enum tag property
            } ?: default
            // ^ Returns the match or defaults to Telugu
        }
        // ^ Ends fromLanguageTag function
    }
    // ^ Ends companion object block
}
// ^ Ends AppLanguage enum class
