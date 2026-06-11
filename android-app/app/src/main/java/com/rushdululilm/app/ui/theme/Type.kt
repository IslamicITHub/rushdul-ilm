// File: Type.kt
// Purpose: Defines the text styles (fonts, sizes, weights) used across the app.
// Layer: Layer 1 — Android App (UI Theme)
// Depends on: None (Standalone UI Configuration)
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.theme

import androidx.compose.material3.Typography
// ^ Material Design 3 Typography configuration class for organizing text styles in styles like title, body, label
import androidx.compose.ui.text.TextStyle
// ^ Jetpack Compose class containing font family, size, weight, line height, and letter spacing properties
import androidx.compose.ui.text.font.FontFamily
// ^ Jetpack Compose class defining the font family (e.g. system default, serif, sans-serif)
import androidx.compose.ui.text.font.FontWeight
// ^ Jetpack Compose class defining font weights (e.g. Bold, Normal, Light, Medium)
import androidx.compose.ui.unit.sp
// ^ Kotlin extension property to declare size-independent pixels (sp) which scale based on user accessibility font size settings

// 🏛️ CONCEPT: Material3 Typography maps common text roles (like bodyLarge, titleLarge) to specific TextStyles.
//    Declaring sizes in scale-independent pixels (sp) ensures accessibility settings are honored.
// 🏛️ ANALOGY: Typography is like a style guide in a publishing house. 
//    Instead of telling writers to make titles "bold and large" manually, the guide defines "titleLarge" and tells everyone to just apply that label.
val Typography = Typography(
// ^ Typography constructor defines a configuration for Material3 text styles
    bodyLarge = TextStyle(
    // ^ bodyLarge represents the style for main content texts (like answers)
        fontFamily = FontFamily.Default,
        // ^ fontFamily sets the font type to the default system font
        fontWeight = FontWeight.Normal,
        // ^ fontWeight sets the font thickness to standard normal weight
        fontSize = 18.sp,
        // ^ fontSize sets the font size to 18sp (highly readable for low-literacy users)
        lineHeight = 28.sp,
        // ^ lineHeight sets the height of a text line to 28sp to prevent lines from crowding each other
        letterSpacing = 0.5.sp
        // ^ letterSpacing adds 0.5sp spacing between characters to improve legibility
    ),
    // ^ Ends bodyLarge TextStyle configuration
    
    bodyMedium = TextStyle(
    // ^ bodyMedium represents the style for secondary text blocks
        fontFamily = FontFamily.Default,
        // ^ fontFamily sets the font type to default system font
        fontWeight = FontWeight.Normal,
        // ^ fontWeight sets the font thickness to standard normal weight
        fontSize = 16.sp,
        // ^ fontSize sets the font size to 16sp (the strict accessibility minimum font size)
        lineHeight = 24.sp,
        // ^ lineHeight sets the line height to 24sp for comfort reading
        letterSpacing = 0.5.sp
        // ^ letterSpacing adds 0.5sp space between characters
    ),
    // ^ Ends bodyMedium TextStyle configuration

    titleLarge = TextStyle(
    // ^ titleLarge represents the style for screen headers and action bar titles
        fontFamily = FontFamily.Default,
        // ^ fontFamily sets the font type to default system font
        fontWeight = FontWeight.Bold,
        // ^ fontWeight sets the font thickness to bold for emphasis
        fontSize = 22.sp,
        // ^ fontSize sets the font size to 22sp for clear readability
        lineHeight = 28.sp,
        // ^ lineHeight sets the line height to 28sp
        letterSpacing = 0.sp
        // ^ letterSpacing removes extra spacing between characters for large titles
    ),
    // ^ Ends titleLarge TextStyle configuration

    labelSmall = TextStyle(
    // ^ labelSmall represents the style for buttons, source badges, and chip selectors
        fontFamily = FontFamily.Default,
        // ^ fontFamily sets the font type to default system font
        fontWeight = FontWeight.Medium,
        // ^ fontWeight sets the font thickness to medium weight
        fontSize = 16.sp,
        // ^ fontSize sets the font size to 16sp (overridden from default Material3 for accessibility)
        lineHeight = 20.sp,
        // ^ lineHeight sets the line height to 20sp
        letterSpacing = 0.5.sp
        // ^ letterSpacing adds 0.5sp space between characters
    )
    // ^ Ends labelSmall TextStyle configuration
)
// ^ Ends the Typography configuration
