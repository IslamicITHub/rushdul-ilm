package com.rushdululilm.app

// File: MainActivity.kt
// Purpose: The main entry point (Front Door) of the app
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Modified: 2026-06-08
// Developer: Shaik Hidayatullah

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.rushdululilm.app.ui.screens.RushdulIlmNavGraph
import com.rushdululilm.app.ui.theme.RushdulIlmTheme
import dagger.hilt.android.AndroidEntryPoint

// 🏛️ We changed ComponentActivity to AppCompatActivity.
// AppCompatActivity provides extra powers, like the ability to change the
// app's language (Locale) while the app is running.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // onCreate is called when the app starts up for the first time
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge makes the app content go behind the status bar
        enableEdgeToEdge()

        // setContent is where we tell Compose to draw our UI
        setContent {
            // RushdulIlmTheme is our custom app look
            RushdulIlmTheme {
                // The NavGraph decides which screen to show
                RushdulIlmNavGraph()
            }
        }
    }
}

