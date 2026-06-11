// File: LanguageSelector.kt
// Purpose: A dropdown menu allowing the user to select their preferred language for questions and answers.
// Layer: Layer 1 — Android App (UI Component)
// Depends on: AppLanguage.kt, strings.xml
// Created: 2026-05-31 | Modified: 2026-06-10
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rushdululilm.app.R
import com.rushdululilm.app.model.AppLanguage

/**
 * A dropdown component to select the app's language.
 *
 * @param selectedLanguage The currently active app language.
 * @param onLanguageSelected A function that gets called when the user picks a new language.
 */
@OptIn(ExperimentalMaterial3Api::class) // ExposedDropdownMenuBox is still marked experimental by Google
@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    // List of available languages from the central AppLanguage model.
    val languages = AppLanguage.entries

    // State to track whether the dropdown menu is currently open (visible) or closed.
    // 'remember' keeps this true/false value alive across screen redraws.
    var expanded by remember { mutableStateOf(false) }

    // Find the localized display string for the currently selected language.
    val displayLanguage = stringResource(selectedLanguage.displayNameRes)

    // ExposedDropdownMenuBox is the Google-recommended way to build a dropdown menu in Material3.
    // It automatically handles the complex logic of opening the menu, positioning it, and closing it when tapped outside.
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }, // Toggle the menu when tapped
        modifier = Modifier.fillMaxWidth()
    ) {
        // The text field that the user actually sees before opening the menu.
        OutlinedTextField(
            value = displayLanguage,
            onValueChange = {}, // Read-only, so we do nothing here
            readOnly = true, // Prevent the keyboard from popping up
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Language, 
                    contentDescription = stringResource(R.string.icon_desc_language)
                ) 
            },
            trailingIcon = { 
                // Automatically draws a tiny down-arrow or up-arrow based on 'expanded' state
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
            },
            modifier = Modifier
                .menuAnchor() // Critical: tells the dropdown where to attach itself
                .fillMaxWidth()
                .heightIn(min = 56.dp), // Enforce minimum touch target height for accessibility
            textStyle = MaterialTheme.typography.bodyLarge, // 18sp minimum for readability
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        // The actual pop-up menu containing the options
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false } // Close if user taps outside
        ) {
            // Loop through our language list and create an item for each one
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = stringResource(language.displayNameRes),
                            style = MaterialTheme.typography.bodyLarge // Large text for menu items too
                        ) 
                    },
                    onClick = {
                        // When tapped, notify the parent and close the menu
                        onLanguageSelected(language)
                        expanded = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp) // Accessibility: min touch target inside menu
                )
            }
        }
    }
}

// Preview to test the UI locally
/*
@Preview(showBackground = true)
@Composable
fun LanguageSelectorPreview() {
    RushdulIlmTheme {
        LanguageSelector(
            selectedLanguage = AppLanguage.ENGLISH,
            onLanguageSelected = {}
        )
    }
}
*/
