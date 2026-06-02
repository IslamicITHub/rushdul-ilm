package com.rushdululilm.app

// File: MainActivity.kt
// Purpose: The main entry point (Front Door) of the app
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rushdululilm.app.ui.screens.RushdulIlmNavGraph
import com.rushdululilm.app.ui.theme.RushdulIlmTheme
import dagger.hilt.android.AndroidEntryPoint

// ComponentActivity is the base class for apps that use Jetpack Compose.
// @AndroidEntryPoint is a magic tag that allows Hilt to provide objects
// (like ViewModels) to this Activity.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // onCreate is called when the app starts up for the first time
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // enableEdgeToEdge makes the app content go behind the status bar (at the top)
        // and the navigation bar (at the bottom) for a modern full-screen look.
        enableEdgeToEdge()
        
        // setContent is where we tell Compose to draw our UI on the screen
        setContent {
            // RushdulIlmTheme is our custom app look (colors, fonts, etc.)
            RushdulIlmTheme {
                // Instead of a single "Hello" screen, we now show our Navigation Graph.
                // The NavGraph will decide which screen to show based on the current route.
                RushdulIlmNavGraph()
            }
        }
    }
}
