package com.rushdululilm.app

// File: RushdulIlmApplication.kt
// Purpose: The main entry point for the entire Android Application process
// Layer: Layer 1 — Android App (UI)
// Depends on: None (Base Android Class)
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import android.app.Application
// ^ Base Android class for maintaining global application state and running initialization code before screens load
import dagger.hilt.android.HiltAndroidApp
// ^ Annotation from the Hilt library that triggers the creation of Hilt's dependency injection container

// 🏛️ CONCEPT: The Application class is the global manager of our app. 
//    It starts running before any of our UI screens exist, and stays alive until the app is closed.
// 🏛️ ANALOGY: The Application class is like the main power grid station of a city. 
//    Before any house (screen) can get electricity, the main power station must be built and turned on.
@HiltAndroidApp
// ^ This annotation acts as the magic "on switch" for Hilt. It generates the global container that supplies dependencies to all screens.
class RushdulIlmApplication : Application() {
// ^ RushdulIlmApplication inherits from the standard Android Application class
    // This class is currently empty, serving as Hilt's global registration point.
}
// ^ Ends the RushdulIlmApplication class
