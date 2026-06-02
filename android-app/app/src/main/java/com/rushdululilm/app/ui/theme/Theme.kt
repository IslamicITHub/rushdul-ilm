// File: Theme.kt
// Purpose: Wraps the colors and typography into a complete Material3 Theme for the app.
// Layer: Layer 1 — Android App (UI Theme)
// Depends on: Color.kt, Type.kt
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 🌗 Color Schemes
// We define how our custom colors map to Material Design's expected color slots.

// For now, we will use a light color scheme primarily, mapping our specific colors.
// If dark mode is added later, we would adjust this map.
private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = BackgroundCream, // Text on top of primary color
    secondary = QuranicBlue,
    error = ErrorRed,
    background = BackgroundCream,
    surface = BackgroundCream, // Surfaces like Cards use the background color
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

// A basic dark scheme mapping (optional, but good practice to include if the user forces dark mode)
private val DarkColorScheme = darkColorScheme(
    primary = IslamicGreen,
    secondary = QuranicBlue,
    background = TextPrimary, // Dark background
    surface = TextPrimary,
    onBackground = BackgroundCream, // Light text on dark background
    onSurface = BackgroundCream
)

/**
 * RushdulIlmTheme is the master style wrapper.
 * Everything inside this function will automatically use the colors and fonts we defined.
 */
@Composable
fun RushdulIlmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but we'll disable it by default 
    // to strictly enforce our branded IslamicGreen and BackgroundCream look.
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
