package com.rushdululilm.app.ui.screens

// File: HomeScreen.kt
// Purpose: The main screen where the user asks questions using the mic.
// Layer: Layer 1 — Android App (UI)
// Depends on: HomeViewModel.kt, MicButton.kt, LanguageSelector.kt, SourceSelector.kt
// Created: 2026-05-30 | Modified: 2026-05-31
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.rushdululilm.app.R
import com.rushdululilm.app.ui.components.LanguageSelector
import com.rushdululilm.app.ui.components.MicButton
import com.rushdululilm.app.ui.components.SourceSelector
import com.rushdululilm.app.ui.theme.OfflineOrange
import com.rushdululilm.app.viewmodel.HomeViewModel

/**
 * HomeScreen is the primary interaction point for the user.
 * It uses MVVM pattern, observing state from [HomeViewModel].
 * 
 * @param navController Navigation controller to move between screens.
 * @param homeViewModel The brain of this screen, provided by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar is still experimental
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel() // Hilt automatically provides the ViewModel
) {
    // --- State Observation ---
    // We observe the "live variables" from the ViewModel.
    // When these change, this Composable function automatically "re-runs" (recomposes).
    
    val selectedLanguage by homeViewModel.selectedLanguage.collectAsState()
    val selectedSource by homeViewModel.selectedSource.collectAsState()
    val isRecording by homeViewModel.isRecording.collectAsState()

    // Fake offline status for now — will be connected to real detection in Phase 4.
    val isOffline = false 

    // Scaffold provides the basic structure (Top Bar, Bottom Bar, Content).
    Scaffold(
        topBar = {
            // A professional header centered in the middle of the bar.
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rushd-ul-Ilm (రشد العلم)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                // Use our theme colors for the top bar
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        // Column stacks elements vertically (one on top of another).
        Column(
            modifier = Modifier
                .fillMaxSize() // Take up the whole screen
                .padding(innerPadding) // Respect the space taken by the TopAppBar
                .background(MaterialTheme.colorScheme.background), // Use our warm cream color
            horizontalAlignment = Alignment.CenterHorizontally // Center children horizontally
        ) {
            
            // 1. Offline Banner (Shown only if isOffline is true)
            if (isOffline) {
                Surface(
                    color = OfflineOrange, // Use our specific orange color
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📵 ఆఫ్‌లైన్ మోడ్ — డౌన్‌లోడ్ చేసిన జ్ఞానాన్ని ఉపయోగిస్తోంది\n📵 Offline Mode — Using Downloaded Knowledge",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall, // 16sp
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Language Selector (Connected to ViewModel)
            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { homeViewModel.onLanguageSelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Source Selector (Connected to ViewModel)
            SourceSelector(
                selectedSource = selectedSource,
                onSourceSelected = { homeViewModel.onSourceSelected(it) }
            )

            // Spacer with weight(1f) acts like a spring, pushing everything else down.
            // But here we want the mic button to be positioned specifically.
            Spacer(modifier = Modifier.weight(1f))

            // 4. Large Microphone Button (The heart of the screen)
            MicButton(
                isRecording = isRecording,
                onClick = { homeViewModel.onMicPressed() }
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // 5. Instruction Text (Telugu + English)
            Text(
                text = stringResource(R.string.mic_button_hint),
                style = MaterialTheme.typography.bodyLarge, // 18sp minimum
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
