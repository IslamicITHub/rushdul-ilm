package com.rushdululilm.app.ui.components

// File: LanguageSelector.kt
// Purpose: A dropdown menu allowing the user to select their preferred language for questions and answers.
// Layer: Layer 1 — Android App (UI Component)
// Depends on: AppLanguage.kt, strings.xml, R
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.layout.fillMaxWidth
// ^ Modifier extension that stretches the width of the layout to match the parent container
import androidx.compose.foundation.layout.heightIn
// ^ Modifier extension to set constraints on minimum and maximum height sizes
import androidx.compose.material.icons.Icons
// ^ Material Design icons registry object
import androidx.compose.material.icons.filled.Language
// ^ Standard vector globe icon representing language settings
import androidx.compose.material3.DropdownMenuItem
// ^ Material3 widget representing a single selectable row inside a dropdown popup menu
import androidx.compose.material3.ExperimentalMaterial3Api
// ^ Opt-in annotation for using under-development Material3 library classes
import androidx.compose.material3.ExposedDropdownMenuBox
// ^ Material3 wrapper layout that binds a text field anchor to a dropdown menu popup
import androidx.compose.material3.ExposedDropdownMenuDefaults
// ^ Material3 class containing standard default styling and widgets for dropdown menus
import androidx.compose.material3.Icon
// ^ Composable widget that draws vector graphic icons with color tints
import androidx.compose.material3.MaterialTheme
// ^ Component providing access to our custom colors, fonts, and shapes theme configurations
import androidx.compose.material3.OutlinedTextField
// ^ Material3 input field widget outlined with a border, used here as the selector display box
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.runtime.getValue
// ^ Kotlin getter extension property for Compose state variables (allows simple var access instead of .value)
import androidx.compose.runtime.mutableStateOf
// ^ Compose function creating a write-support state holder containing a starting value
import androidx.compose.runtime.remember
// ^ Compose state wrapper that preserves values across recomposition redraws
import androidx.compose.runtime.setValue
// ^ Kotlin setter extension property for Compose state variables (allows direct variable assignment instead of .value)
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.res.stringResource
// ^ Compose utility function to load localized string texts from strings.xml at runtime
import androidx.compose.ui.unit.dp
// ^ Extension property converting numbers to density-independent pixels (dp) for screen density scaling
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry
import com.rushdululilm.app.model.AppLanguage
// ^ Imports the central supported language Enum definition

// 🏛️ CONCEPT: Dropdown menus in Compose use ExposedDropdownMenuBox. 
//    An OutlinedTextField acts as the anchor read-only display, and ExposedDropdownMenu renders the list of choices when clicked.
// 🏛️ ANALOGY: LanguageSelector is like a rolled-up classroom wall map. 
//    Initially, it only shows the title header. When you pull the tab (tap the field), it rolls down (opens menu) to reveal all country names (language items).
@OptIn(ExperimentalMaterial3Api::class)
// ^ Opts in to using Experimental Material3 dropdown layout APIs
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun LanguageSelector(
// ^ Declares LanguageSelector function
    selectedLanguage: AppLanguage,
    // ^ parameter representing the active language state
    onLanguageSelected: (AppLanguage) -> Unit
    // ^ parameter callback invoked when the user selects a new language option
) {
// ^ Starts LanguageSelector body
    val languages = AppLanguage.entries
    // ^ Retrieves the list of all supported AppLanguage enums

    var expanded by remember { mutableStateOf(false) }
    // ^ state boolean variable tracking whether the dropdown popup list is open or closed (starts as false)

    val displayLanguage = stringResource(selectedLanguage.displayNameRes)
    // ^ Resolves the active selected language display name from resource file strings.xml

    ExposedDropdownMenuBox(
    // ^ Container coordinating anchor text field clicks to dropdown popup menu visibility
        expanded = expanded,
        // ^ Passes active menu open state
        onExpandedChange = { expanded = it },
        // ^ Toggles the expanded open state when the text field container is clicked
        modifier = Modifier.fillMaxWidth()
        // ^ Configures the container to fill the screen width
    ) {
    // ^ Starts ExposedDropdownMenuBox body
        OutlinedTextField(
        // ^ Draws the outlined selection box displaying the active language
            value = displayLanguage,
            // ^ Passes resolved active language name string
            onValueChange = {},
            // ^ Empty callback as the field is read-only
            readOnly = true,
            // ^ Disables soft keyboard popups when user clicks the box
            leadingIcon = {
            // ^ Passes leading widget block (drawn on the left side of text)
                Icon(
                // ^ Draws globe icon
                    imageVector = Icons.Default.Language,
                    // ^ standard Language vector graphic
                    contentDescription = stringResource(R.string.icon_desc_language)
                    // ^ screen reader content description string
                )
                // ^ Ends Icon widget
            },
            // ^ Ends leadingIcon parameter
            trailingIcon = {
            // ^ Passes trailing widget block (drawn on the right side of text)
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                // ^ Draws standard rotating dropdown arrow arrow icon based on active expanded state
            },
            // ^ Ends trailingIcon parameter
            modifier = Modifier
            // ^ Instantiates modifier builder
                .menuAnchor()
                // ^ Critical mapping attaching the menu popup layout to the bottom of this text field
                .fillMaxWidth()
                // ^ Expands width to match parent Column width
                .heightIn(min = 56.dp),
                // ^ Enforces 56dp height constraint to fulfill touch target size accessibility requirements
            textStyle = MaterialTheme.typography.bodyLarge,
            // ^ Applies 18sp typography size for high readability
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            // ^ Applies standard outlined text field color scheme configurations
        )
        // ^ Ends OutlinedTextField widget

        ExposedDropdownMenu(
        // ^ Renders the popup options list overlay window
            expanded = expanded,
            // ^ Passes active open state
            onDismissRequest = { expanded = false }
            // ^ Closes the popup menu overlay if the user taps outside the menu bounds
        ) {
        // ^ Starts ExposedDropdownMenu body
            languages.forEach { language ->
            // ^ Iterates through the list of enum language constants
                DropdownMenuItem(
                // ^ Draws a single dropdown choice list item row
                    text = {
                    // ^ Passes text title widget block
                        Text(
                        // ^ Draws option label text
                            text = stringResource(language.displayNameRes),
                            // ^ Resolves display name string from strings.xml
                            style = MaterialTheme.typography.bodyLarge
                            // ^ Applies 18sp typography text style
                        )
                        // ^ Ends Text widget
                    },
                    // ^ Ends text parameter
                    onClick = {
                    // ^ Callback executed when the user taps this choice item
                        onLanguageSelected(language)
                        // ^ Invokes the parent callback, updating the active language configuration
                        expanded = false
                        // ^ Closes the dropdown menu popup window
                    },
                    // ^ Ends onClick parameter
                    modifier = Modifier.heightIn(min = 48.dp)
                    // ^ Enforces 48dp minimum item height for comfortable touch selection
                )
                // ^ Ends DropdownMenuItem widget
            }
            // ^ Ends iteration block
        }
        // ^ Ends ExposedDropdownMenu widget
    }
    // ^ Ends ExposedDropdownMenuBox widget
}
// ^ Ends LanguageSelector Composable function
