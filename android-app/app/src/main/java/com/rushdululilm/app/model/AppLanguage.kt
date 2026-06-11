// File: AppLanguage.kt
// Purpose: Defines every app language in one modular place so language choices stay consistent across screens.
// Layer: Layer 1 — Android App (Model)
// Depends on: R.string language labels
// Created: 2026-06-10 | Modified: 2026-06-10
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.model

import androidx.annotation.StringRes
import com.rushdululilm.app.R

/**
 * AppLanguage is the official list of languages supported by Rushd-ul-Ilm.
 *
 * @property key Stable app key used by ViewModels and UI state.
 * @property languageTag Android locale tag used by AppCompatDelegate.
 * @property displayNameRes String resource shown to the user in selectors.
 */
enum class AppLanguage(
    val key: String,
    val languageTag: String,
    @StringRes val displayNameRes: Int
) {
    TELUGU(
        key = "Telugu",
        languageTag = "te",
        displayNameRes = R.string.language_telugu_display
    ),
    URDU(
        key = "Urdu",
        languageTag = "ur",
        displayNameRes = R.string.language_urdu_display
    ),
    HINDI(
        key = "Hindi",
        languageTag = "hi",
        displayNameRes = R.string.language_hindi_display
    ),
    ENGLISH(
        key = "English",
        languageTag = "en",
        displayNameRes = R.string.language_english_display
    );

    companion object {
        val default: AppLanguage = TELUGU

        fun fromKey(key: String): AppLanguage {
            return entries.firstOrNull { language ->
                language.key == key
            } ?: default
        }

        fun fromLanguageTag(languageTag: String?): AppLanguage {
            val normalizedLanguageTag = languageTag
                ?.substringBefore("-")
                ?.substringBefore("_")

            return entries.firstOrNull { language ->
                language.languageTag == normalizedLanguageTag
            } ?: default
        }
    }
}
