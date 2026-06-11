package com.rushdululilm.app

// File: MainActivity.kt
// Purpose: The main entry point (Front Door) of the app
// Layer: Layer 1 — Android App (UI)
// Depends on: RushdulIlmNavGraph.kt, RushdulIlmTheme.kt
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import android.os.Bundle
// ^ Android library that lets us pass data between screens and save state when the screen rotates
import androidx.activity.compose.setContent
// ^ Jetpack Compose library function that lets us write UI using Kotlin instead of old XML files
import androidx.activity.enableEdgeToEdge
// ^ Android library function to enable edge-to-edge drawing, making the UI fill the entire screen under status bars
import androidx.appcompat.app.AppCompatActivity
// ^ Base class that supports older Android versions and allows runtime language switching (locale updates)
import com.rushdululilm.app.ui.screens.RushdulIlmNavGraph
// ^ Imports our navigation graph to manage moving between different screens in the app
import com.rushdululilm.app.ui.theme.RushdulIlmTheme
// ^ Imports our app's visual style theme (colors, typography, etc.)
import dagger.hilt.android.AndroidEntryPoint
// ^ Tells Hilt (dependency injection tool) to prepare this activity so we can inject ViewModels or Repositories here

// 🏛️ CONCEPT: MainActivity is the first page of our app that the system launches.
//    It acts as the host container where Jetpack Compose draws our screens.
// 🏛️ ANALOGY: MainActivity is like the main stage in a theater. 
//    Without it, the actors (our screens) have no place to stand. It provides the window for the app.
@AndroidEntryPoint
// ^ Annotation telling the compiler that this class will receive dependencies from Hilt
class MainActivity : AppCompatActivity() {
// ^ MainActivity inherits from AppCompatActivity to support dynamic language switching and backward compatibility

    override fun onCreate(savedInstanceState: Bundle?) {
    // ^ onCreate is the starting function called by the Android system when our app is opened
    // ^ savedInstanceState is a bundle that holds the state of the app if it was closed and recreated
        super.onCreate(savedInstanceState)
        // ^ Calls the base AppCompatActivity's onCreate function to let Android set up internal settings

        enableEdgeToEdge()
        // ^ Tells Android to draw the app behind the status bar and navigation bar for a modern look

        setContent {
        // ^ setContent is a Compose block where we define what UI elements to show on the screen
            RushdulIlmTheme {
            // ^ RushdulIlmTheme wraps the UI elements inside our custom colors and fonts
                RushdulIlmNavGraph()
                // ^ Calls our navigation manager to decide and draw the active screen (e.g. Home Screen)
            }
            // ^ Ends the RushdulIlmTheme block
        }
        // ^ Ends the setContent block
    }
    // ^ Ends the onCreate function
}
// ^ Ends the MainActivity class
