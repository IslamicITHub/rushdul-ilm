// File: Type.kt
// Purpose: Defines the text styles (fonts, sizes, weights) used across the app.
// Layer: Layer 1 — Android App (UI Theme)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 📝 App Typography
// We define our text styles here. This ensures that a "title" looks the same on every screen.
// VERY IMPORTANT RULE: Minimum font size for user-facing text is 16sp for accessibility.

val Typography = Typography(
    // Large body text for main content (like the actual fatwa answer)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Medium body text for secondary information (minimum allowed size)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // Large title for screen headers or major sections
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // Small label for buttons or chips (minimum allowed size)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // Note: Default Material is smaller, but our accessibility rule enforces 16sp minimum
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
)
