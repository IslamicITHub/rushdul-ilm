package com.rushdululilm.app.ui.screens

// File: Routes.kt
// Purpose: Defines all the navigation "addresses" (routes) for the app in one place.
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

/**
 * Think of Jetpack Compose Navigation like a city map. 
 * Every screen is a different house, and every house needs an address.
 * 
 * Instead of typing "home" or "settings" manually every time we want to change screens,
 * we store them here as constants (values that never change). 
 * This prevents spelling mistakes and makes the code cleaner.
 */
object Routes {
    const val HOME = "home"           // The main starting screen with the big mic button
    const val ANSWER = "answer"       // The screen that shows the fatwa answer + source URL
    const val VIDEO_LIBRARY = "video_library"  // The screen showing a searchable list of Islamic video lectures
    const val SETTINGS = "settings"   // The screen for app preferences and downloading offline databases
}
