package com.rushdululilm.app.ui.screens

// File: Routes.kt
// Purpose: Defines all the navigation "addresses" (routes) for the app in one place.
// Layer: Layer 1 — Android App (UI)
// Depends on: None (Standalone Configuration)
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

// 🏛️ CONCEPT: Routes is a central registry containing the names of all screens in the app.
//    By centralizing these strings, we ensure that navigating to a screen always uses the correct address.
// 🏛️ ANALOGY: Routes is like a contact list in your phone. Instead of typing a phone number
//    manually every time you want to dial, you tap the contact's name (like "HOME" or "SETTINGS").
object Routes {
// ^ object creates a Singleton (a single static class instance) in Kotlin
    const val HOME = "home"
    // ^ const val defines a read-only constant value known at compile time
    // ^ HOME is the destination address string for the Home screen (with the mic button)
    
    const val ANSWER = "answer"
    // ^ const val defines a read-only constant value known at compile time
    // ^ ANSWER is the destination address string for the screen showing RAG answers
    
    const val VIDEO_LIBRARY = "video_library"
    // ^ const val defines a read-only constant value known at compile time
    // ^ VIDEO_LIBRARY is the destination address string for the video library screen
    
    const val SETTINGS = "settings"
    // ^ const val defines a read-only constant value known at compile time
    // ^ SETTINGS is the destination address string for the app preferences and offline databases
}
// ^ Ends the Routes object
