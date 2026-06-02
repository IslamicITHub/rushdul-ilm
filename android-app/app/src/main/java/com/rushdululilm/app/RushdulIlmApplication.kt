package com.rushdululilm.app

// File: RushdulIlmApplication.kt
// Purpose: The main entry point for the entire Android Application process
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The Application class is the "Big Boss" of the app. 
 * It starts before any screen is shown and lives until the app is fully closed.
 * 
 * @HiltAndroidApp is a magic "Power On" button for Hilt.
 * It tells Hilt to get ready to provide objects (dependencies) to the rest of the app.
 * Without this, Hilt won't work!
 */
@HiltAndroidApp
class RushdulIlmApplication : Application() {
    // This class is empty for now, but it must exist so Hilt has a place to start.
}
