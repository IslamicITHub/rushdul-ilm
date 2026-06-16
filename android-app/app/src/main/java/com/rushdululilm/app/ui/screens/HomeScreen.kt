package com.rushdululilm.app.ui.screens

// File: HomeScreen.kt
// Purpose: The main screen where the user asks questions using the mic.
// Layer: Layer 1 — Android App (UI)
// Depends on: HomeViewModel.kt, MicButton.kt, LanguageSelector.kt, SourceSelector.kt, Routes.kt
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
// ^ Compose function modifier to draw solid colors behind layout content
import androidx.compose.foundation.layout.*
// ^ Imports standard Compose layouts (Column, Row, Box, Spacer) and dimensional paddings
import androidx.compose.material3.CenterAlignedTopAppBar
// ^ Material3 container for screen headers, centering the title text automatically
import androidx.compose.material3.ExperimentalMaterial3Api
// ^ Opt-in annotation required by Material3 design components that are still under development
import androidx.compose.material3.MaterialTheme
// ^ Component providing access to our custom colors, fonts, and shapes theme configurations
import androidx.compose.material3.Scaffold
// ^ Layout structural component managing top bars, bottom navigation bar, and main page content spacing
import androidx.compose.material3.Surface
// ^ Material3 layout drawing surface that handles background color overlays and round corner shapes
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.material3.TopAppBarDefaults
// ^ Material3 class containing style configurations for action and top bars
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.runtime.collectAsState
// ^ Extension function that maps Kotlin StateFlow values into Compose State to trigger screen redraws
import androidx.compose.runtime.getValue
// ^ Kotlin getter extension property for Compose state variables (allows simple var access instead of .value)
import androidx.compose.ui.Alignment
// ^ Layout configuration for positioning items (e.g. Center, TopStart, BottomEnd)
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.graphics.Color
// ^ Compose class defining colors using ARGB Hex values
import androidx.compose.ui.text.font.FontWeight
// ^ Compose class defining character weight thickness (e.g. Bold, Normal)
import androidx.compose.ui.text.style.TextAlign
// ^ Compose class detailing text alignment (e.g. Center, Justify, Left)
import androidx.compose.ui.unit.dp
// ^ Extension property converting numbers to density-independent pixels (dp) for screen density scaling
import androidx.compose.ui.unit.sp
// ^ Extension property converting numbers to scale-independent pixels (sp) for system font scaling
import androidx.hilt.navigation.compose.hiltViewModel
// ^ Dagger Hilt extension function that automatically creates or loads ViewModels for screens
import androidx.navigation.NavController
// ^ Class managing navigation actions and backing histories between app screens
import androidx.compose.ui.res.stringResource
// ^ Compose utility function to load localized string texts from strings.xml at runtime
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry
import com.rushdululilm.app.ui.components.LanguageSelector
// ^ Imports the reusable custom dropdown selector component for languages
import com.rushdululilm.app.ui.components.MicButton
// ^ Imports the animated high-contrast microphone button component
import com.rushdululilm.app.ui.components.SourceSelector
// ^ Imports the category chips selector component for Islamic database search filters
import androidx.compose.runtime.LaunchedEffect
// ^ Compose side-effect block to execute actions (like navigation) safely when states change
import androidx.compose.material3.CircularProgressIndicator
// ^ Material3 circular progress loader indicator
import com.rushdululilm.app.ui.theme.IslamicGreen
// ^ Custom primary green color from our design theme
import com.rushdululilm.app.ui.theme.OfflineOrange
// ^ Custom orange color used for offline warning states
import com.rushdululilm.app.viewmodel.HomeUiState
// ^ Imports the Home Screen state hierarchy sealed class
import com.rushdululilm.app.viewmodel.HomeViewModel
// ^ Imports the ViewModel manager class of this screen

// 🏛️ CONCEPT: Composable functions (annotated with @Composable) are the building blocks of Jetpack Compose.
//    They take data from StateFlow streams and draw UI elements on the screen. Whenever state changes, the function recomposes automatically.
// 🏛️ ANALOGY: HomeScreen is like a service desk window. 
//    The user picks their language (dropdown) and source filters (chips), then rings the bell (mic button). The desk shows a spinner (Processing) and opens the result window (AnswerScreen) once ready.
@OptIn(ExperimentalMaterial3Api::class)
// ^ Tells compiler we are choosing to use Experimental Material3 TopAppBar API features
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun HomeScreen(
// ^ Declares HomeScreen function
    navController: NavController,
    // ^ Parameter carrying the NavController to route users to the Answer screen
    homeViewModel: HomeViewModel = hiltViewModel()
    // ^ Parameter supplying the brain ViewModel instance, injected by Hilt by default
) {
// ^ Starts HomeScreen body

    val selectedLanguage by homeViewModel.selectedLanguage.collectAsState()
    // ^ Observes selectedLanguage StateFlow from ViewModel, mapping it to a standard variable delegate
    
    val selectedSource by homeViewModel.selectedSource.collectAsState()
    // ^ Observes selectedSource StateFlow from ViewModel to update active selector chips
    
    val isRecording by homeViewModel.isRecording.collectAsState()
    // ^ Observes isRecording flow to animate the mic button pulse effect
    
    val uiState by homeViewModel.uiState.collectAsState()
    // ^ Observes uiState flow to trigger navigation loaders or error message banners

    LaunchedEffect(uiState) {
    // ^ Launches a side-effect block that runs immediately whenever uiState changes
        if (uiState is HomeUiState.NavigatingToAnswer) {
        // ^ Checks if the UI state has transitioned to NavigatingToAnswer
            navController.navigate(Routes.ANSWER)
            // ^ Tells the NavController to load and draw the Answer screen
            
            homeViewModel.resetUiState()
            // ^ Resets the ViewModel state back to Idle so back navigation works correctly
        }
        // ^ Ends validation check
    }
    // ^ Ends LaunchedEffect block

    val isOffline = false
    // ^ Local placeholder boolean tracking connectivity status

    Scaffold(
    // ^ Sets up standard screen scaffolding layout
        topBar = {
        // ^ Passes topBar widget block
            CenterAlignedTopAppBar(
            // ^ Instantiates centered header bar
                title = {
                // ^ Passes title widget block
                    Text(
                    // ^ Draws header title text
                        text = stringResource(R.string.home_screen_label),
                        // ^ Fetches bilingual screen title from strings.xml
                        fontWeight = FontWeight.Bold,
                        // ^ Sets text font thickness to bold
                        style = MaterialTheme.typography.titleLarge
                        // ^ Applies accessibility titleLarge style template
                    )
                    // ^ Ends Text widget
                },
                // ^ Ends title block
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                // ^ Instantiates color configurations for the top bar
                    containerColor = MaterialTheme.colorScheme.primary,
                    // ^ Sets background color to our primary theme color (IslamicGreen)
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                    // ^ Sets text color to our high-contrast onPrimary color
                )
                // ^ Ends colors parameter
            )
            // ^ Ends CenterAlignedTopAppBar
        }
        // ^ Ends topBar block
    ) { innerPadding ->
    // ^ innerPadding parameter provides layout margins so body widgets do not overlap the topBar
        Column(
        // ^ Column aligns layout items vertically from top to bottom
            modifier = Modifier
            // ^ Instantiates modifier builder
                .fillMaxSize() 
                // ^ Configures the column to expand to fill the entire screen space
                .padding(innerPadding) 
                // ^ Adds margins matching the innerPadding height offsets
                .background(MaterialTheme.colorScheme.background), 
                // ^ Sets background color to our warm Cream color
            horizontalAlignment = Alignment.CenterHorizontally 
            // ^ Centers all children widgets horizontally in the column
        ) {
        // ^ Starts Column body
            
            if (isOffline) {
            // ^ Checks if offline status is active
                Surface(
                // ^ Instantiates background drawing surface
                    color = OfflineOrange, 
                    // ^ Sets surface background to OfflineOrange warning color
                    modifier = Modifier.fillMaxWidth()
                    // ^ Sets modifier to fill screen width
                ) {
                // ^ Starts Surface body
                    Text(
                    // ^ Draws offline warning banner text
                        text = stringResource(R.string.offline_banner_text),
                        // ^ Fetches bilingual offline warning message from strings.xml
                        modifier = Modifier.padding(8.dp),
                        // ^ Adds 8dp padding around the text content
                        textAlign = TextAlign.Center,
                        // ^ Centers the text inside the banner bounds
                        style = MaterialTheme.typography.labelSmall, 
                        // ^ Applies labelSmall font style (16sp accessibility minimum)
                        color = Color.White
                        // ^ Sets text color to solid White
                    )
                    // ^ Ends Text widget
                }
                // ^ Ends Surface widget
            }
            // ^ Ends offline check block

            Spacer(modifier = Modifier.height(16.dp))
            // ^ Adds a blank spacing gap of 16dp below the top bar

            LanguageSelector(
            // ^ Instantiates language dropdown selector component
                selectedLanguage = selectedLanguage,
                // ^ Passes active selected language property
                onLanguageSelected = { homeViewModel.onLanguageSelected(it) }
                // ^ Passes callback updating ViewModel state when user clicks a language item
            )
            // ^ Ends LanguageSelector widget

            Spacer(modifier = Modifier.height(12.dp))
            // ^ Adds a blank spacing gap of 12dp below the language selector

            SourceSelector(
            // ^ Instantiates source preferences chips selector component
                selectedSource = selectedSource,
                // ^ Passes active selected source filter value
                onSourceSelected = { homeViewModel.onSourceSelected(it) }
                // ^ Passes callback updating ViewModel state when user clicks a chip
            )
            // ^ Ends SourceSelector widget

            Spacer(modifier = Modifier.weight(1f))
            // ^ Adds a dynamic spacing spring pushing widgets below it down to bottom of screen

            if (uiState is HomeUiState.Processing) {
            // ^ Checks if API network request is active
                Box(
                // ^ Instantiates layout box container
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    // ^ Sets container modifier height to 150dp and matches parent width
                    contentAlignment = Alignment.Center
                    // ^ Centers children widgets inside the box bounds
                ) {
                // ^ Starts Box body
                    CircularProgressIndicator(color = IslamicGreen)
                    // ^ Draws standard loading spinner widget styled in IslamicGreen
                }
                // ^ Ends Box widget
            } else {
            // ^ Runs if app is idle or recording
                MicButton(
                // ^ Instantiates main microphone button component (40% height animated layout)
                    isRecording = isRecording,
                    // ^ Passes active recording state boolean
                    onClick = { homeViewModel.onMicPressed() }
                    // ^ Passes click event callback to toggle recording state
                )
                // ^ Ends MicButton widget
            }
            // ^ Ends loading check block

            Spacer(modifier = Modifier.weight(0.5f))
            // ^ Adds a spacing spring below the mic button for layout padding

            val instructionText = if (uiState is HomeUiState.Error) {
            // ^ Determines what instructions text to display
                (uiState as HomeUiState.Error).message
                // ^ Displays the custom error message if screen state is Error
            } else {
            // ^ Otherwise runs standard hint message
                stringResource(R.string.mic_button_hint)
                // ^ Fetches bilingual tap-to-speak user guidance label from strings.xml
            }
            // ^ Ends instructionText selection
            
            Text(
            // ^ Draws user instruction guidance label text
                text = instructionText,
                // ^ Passes computed instruction text
                style = MaterialTheme.typography.bodyLarge, 
                // ^ Applies bodyLarge typography style (18sp accessibility size)
                color = if (uiState is HomeUiState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                // ^ Sets text color to red in error states, otherwise default theme text color
                textAlign = TextAlign.Center,
                // ^ Centers text layout bounds
                lineHeight = 24.sp,
                // ^ Sets line height spacing to 24sp for accessibility readability
                modifier = Modifier
                // ^ Instantiates modifier builder
                    .padding(horizontal = 16.dp)
                    // ^ Adds 16dp padding to the left and right margins
                    .padding(bottom = 32.dp)
                    // ^ Adds 32dp padding to the bottom margin
            )
            // ^ Ends Text widget
        }
        // ^ Ends Column body
    }
    // ^ Ends Scaffold content block
}
// ^ Ends HomeScreen Composable function
