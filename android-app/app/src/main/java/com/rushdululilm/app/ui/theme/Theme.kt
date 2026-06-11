// File: Theme.kt
// Purpose: Wraps the colors and typography into a complete Material3 Theme for the app.
// Layer: Layer 1 — Android App (UI Theme)
// Depends on: Color.kt, Type.kt
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.theme

import android.os.Build
// ^ Android library class that lets us check the device's Android OS version at runtime
import androidx.compose.foundation.isSystemInDarkTheme
// ^ Jetpack Compose function that detects if the user has enabled system-wide dark mode in Android settings
import androidx.compose.material3.MaterialTheme
// ^ Jetpack Compose Material3 master design component that applies color, typography, and shapes to its children
import androidx.compose.material3.darkColorScheme
// ^ Material3 function that helper builds a dark mode color palette
import androidx.compose.material3.dynamicDarkColorScheme
// ^ Material3 helper to generate a dynamic dark color scheme based on user wallpaper (Android 12+)
import androidx.compose.material3.dynamicLightColorScheme
// ^ Material3 helper to generate a dynamic light color scheme based on user wallpaper (Android 12+)
import androidx.compose.material3.lightColorScheme
// ^ Material3 function that helper builds a light mode color palette
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.ui.platform.LocalContext
// ^ Jetpack Compose utility to retrieve the Android Context (used to load resources or query system settings)

// 🏛️ CONCEPT: A Theme coordinates a unified design system. We map our custom colors (like IslamicGreen)
//    to standard semantic roles (like primary, error, background) so standard Compose buttons and screens style themselves automatically.
// 🏛️ ANALOGY: Theme.kt is like a uniform dress code for a school. 
//    Instead of letting every student choose their own colors, the theme defines the official colors, and everyone who puts on the uniform matches.

private val LightColorScheme = lightColorScheme(
// ^ private val defines a read-only variable visible only inside this file
// ^ lightColorScheme configures colors for light background environments
    primary = IslamicGreen,
    // ^ Maps our custom IslamicGreen to the primary brand color slot
    onPrimary = BackgroundCream,
    // ^ Maps BackgroundCream for text drawn on top of primary elements (for high visibility contrast)
    secondary = QuranicBlue,
    // ^ Maps QuranicBlue to the secondary accent slot
    error = ErrorRed,
    // ^ Maps ErrorRed to error message displays
    background = BackgroundCream,
    // ^ Maps BackgroundCream to the main background slot
    surface = BackgroundCream,
    // ^ Maps BackgroundCream to cards and dialog container backgrounds
    onBackground = TextPrimary,
    // ^ Maps TextPrimary (soft black) to body text drawn on backgrounds
    onSurface = TextPrimary
    // ^ Maps TextPrimary (soft black) to body text drawn on cards/surfaces
)
// ^ Ends LightColorScheme configuration

private val DarkColorScheme = darkColorScheme(
// ^ private val defines a read-only variable visible only inside this file
// ^ darkColorScheme configures colors for dark background environments
    primary = IslamicGreen,
    // ^ Keeps primary color as IslamicGreen
    secondary = QuranicBlue,
    // ^ Keeps secondary color as QuranicBlue
    background = TextPrimary,
    // ^ Maps TextPrimary (soft black) to dark background slot
    surface = TextPrimary,
    // ^ Maps TextPrimary to card backgrounds in dark mode
    onBackground = BackgroundCream,
    // ^ Maps BackgroundCream to readable text drawn on dark background
    onSurface = BackgroundCream
    // ^ Maps BackgroundCream to readable text drawn on dark card surfaces
)
// ^ Ends DarkColorScheme configuration

@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun RushdulIlmTheme(
// ^ Defines our custom theme wrapper function
    darkTheme: Boolean = isSystemInDarkTheme(),
    // ^ parameter checks if the device is currently running dark theme (defaults to system setting)
    dynamicColor: Boolean = false,
    // ^ parameter turns dynamic wallpaper color styling on or off (defaults to false for brand consistency)
    content: @Composable () -> Unit
    // ^ parameter takes a function block representing the UI widgets to draw inside the theme
) {
// ^ Starts RushdulIlmTheme implementation
    val colorScheme = when {
    // ^ matches conditions to determine which color scheme to apply
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        // ^ Check if wallpaper-based dynamic colors are allowed and device is Android 12+ (SDK 31+)
            val context = LocalContext.current
            // ^ Gets the Context of the current screen window
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            // ^ Applies dynamic dark colors if system is dark mode, otherwise dynamic light colors
        }
        // ^ Ends dynamic color matching block
        darkTheme -> DarkColorScheme
        // ^ Applies our defined DarkColorScheme if darkTheme is true
        else -> LightColorScheme
        // ^ Otherwise defaults to our defined LightColorScheme
    }
    // ^ Ends val colorScheme matching expression

    MaterialTheme(
    // ^ Calls the framework's MaterialTheme drawing block
        colorScheme = colorScheme,
        // ^ Passes our chosen colorScheme map
        typography = Typography,
        // ^ Passes our accessibility-friendly Typography configuration
        content = content
        // ^ Passes the screen child elements to be styled and rendered
    )
    // ^ Ends MaterialTheme block
}
// ^ Ends the RushdulIlmTheme function
