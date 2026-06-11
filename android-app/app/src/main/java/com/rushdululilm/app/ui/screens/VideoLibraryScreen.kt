package com.rushdululilm.app.ui.screens
// ^ Package declaration defining the namespace where this file lives in the application

// File: VideoLibraryScreen.kt
// Purpose: Displays a list of offline Islamic video lectures that the user can play.
// Layer: Layer 1 — Android App (UI)
// Depends on: VideoLibraryViewModel.kt, VideoCard.kt
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
// ^ Compose function modifier to draw solid colors behind layout content
import androidx.compose.foundation.layout.*
// ^ Imports standard Compose layouts (Column, Row, Box, Spacer) and dimensional paddings
import androidx.compose.foundation.lazy.LazyColumn
// ^ Highly efficient vertical list component that draws only items currently visible on the screen
import androidx.compose.foundation.lazy.items
// ^ Compose extension helper function to loop through Kotlin lists inside LazyColumn/LazyRow blocks
import androidx.compose.foundation.shape.RoundedCornerShape
// ^ Layout builder class used to round card corners and make buttons look like smooth pills
import androidx.compose.material.icons.Icons
// ^ Object containing the standard catalog of Material design system graphic vector icons
import androidx.compose.material.icons.filled.Clear
// ^ Material design icon representing 'X' or 'Clear' to reset search input fields
import androidx.compose.material.icons.filled.Search
// ^ Material design icon representing magnifying glass search to indicate query inputs
import androidx.compose.material3.*
// ^ Imports all core components from Google's Material Design 3 library (TopAppBar, Button, Text, Card)
import androidx.compose.runtime.Composable
// ^ Compiler annotation signaling that this function constructs drawing operations in Compose
import androidx.compose.runtime.collectAsState
// ^ Extension mapping StateFlow dynamic streams into Compose State objects to trigger automatic UI redraws
import androidx.compose.runtime.getValue
// ^ Kotlin property delegate getter enabling direct read of Compose state values without calling .value
import androidx.compose.ui.Modifier
// ^ Main builder class used to chain layout sizes, padding margins, shadow depths, and click events
import androidx.compose.ui.graphics.Color
// ^ Graphics color representation class supporting 32-bit Hex integer values
import androidx.compose.ui.text.font.FontWeight
// ^ Type style class defining standard character weight values (like Bold, Medium, Normal)
import androidx.compose.ui.unit.dp
// ^ Converts standard numbers into density-independent pixels for device display scaling
import androidx.compose.ui.unit.sp
// ^ Converts standard numbers into scale-independent pixels for user font-size configurations
import androidx.hilt.navigation.compose.hiltViewModel
// ^ Dagger Hilt integration function that automatically creates or loads viewmodels scoped to navigation
import androidx.navigation.NavController
// ^ Class managing navigation actions and backing histories between app screens
import androidx.compose.ui.res.stringResource
// ^ Composable helper fetching localized string texts from the values XML resources at runtime
import com.rushdululilm.app.R
// ^ Generated Android resources registry mapping references to raw string and layouts indices
import com.rushdululilm.app.ui.components.VideoCard
// ^ Imports custom video list item layout card component
import com.rushdululilm.app.viewmodel.VideoLibraryViewModel
// ^ Brain ViewModel class that processes video database searches and queries local metadata SQLite tables

// 🏛️ CONCEPT: Search query filtering processes input string modifications asynchronously.
//    Updating search text flows triggers search logic in the background, updating list items.
// 🏛️ ANALOGY: VideoLibraryScreen is like a digital library catalog cabinet.
//    It has a text search drawer (Search Bar) at the top.
//    Typing a word filters the shelves (LazyColumn), showing only video canisters (VideoCards) matching that topic.
@OptIn(ExperimentalMaterial3Api::class)
// ^ Tells compiler we are opting into Experimental Material 3 APIs (like TopAppBar configuration)
@Composable
// ^ Annotation identifying this function as a Compose layout drawing interface block
fun VideoLibraryScreen(
// ^ Declares VideoLibraryScreen composable entry point
    navController: NavController,
    // ^ Parameter carrying NavController for navigating back to previous screens
    viewModel: VideoLibraryViewModel = hiltViewModel()
    // ^ Supplies the active VideoLibraryViewModel instance injected by Hilt by default
) {
// ^ Starts VideoLibraryScreen body block

    val searchQuery by viewModel.searchQuery.collectAsState()
    // ^ Observes searchQuery StateFlow from ViewModel to update active search text field input
    
    val filteredVideos by viewModel.filteredVideos.collectAsState()
    // ^ Binds filteredVideos list changes to refresh scrolling list cards dynamically

    Scaffold(
    // ^ Structural layout widget managing standard material screen scaffold elements
        topBar = {
        // ^ Attaches top app bar header section to the Scaffold layout
            TopAppBar(
            // ^ Displays standard Material 3 screen header bar
                title = { 
                // ^ Title configuration block
                    Text(
                    // ^ Draws main header text
                        text = stringResource(R.string.video_library_label),
                        // ^ Loads localized video library title from XML strings
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        // ^ Applies bold 22sp title typography
                    ) 
                    // ^ Ends Text title widget
                },
                // ^ Ends title mapping
                colors = TopAppBarDefaults.topAppBarColors(
                // ^ Configures color themes applied to header bar contents
                    containerColor = MaterialTheme.colorScheme.primary,
                    // ^ Sets background of top bar to our main primary theme color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                    // ^ Sets title text color to contrast color on primary background
                )
                // ^ Ends color configuration mapping
            )
            // ^ Ends TopAppBar composable definition
        }
        // ^ Ends topBar configuration
    ) { innerPadding ->
    // ^ Exposes padding spacing calculations to prevent content overlap with headers/footers
        Column(
        // ^ Arranges search bar and list container vertically
            modifier = Modifier
            // ^ Chain modifiers to style the main content column canvas
                .fillMaxSize()
                // ^ Stretches column size to occupy total available column canvas space
                .padding(innerPadding)
                // ^ Subtracts header/footer bar height margins from active canvas area
                .background(MaterialTheme.colorScheme.background)
                // ^ Draws background color matches behind scrolling items
        ) {
        // ^ Starts Column layout body content
            Box(
            // ^ Container box styling background behind search text field
                modifier = Modifier
                // ^ Formats search area padding and backgrounds
                    .fillMaxWidth()
                    // ^ Stretches container across screen width bounds
                    .background(MaterialTheme.colorScheme.primary)
                    // ^ Colors search bar wrapper primary theme green
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // ^ Adds 16dp horizontal and 8dp vertical margins surrounding search field
            ) {
            // ^ Starts Box layout
                OutlinedTextField(
                // ^ Material 3 input text field widget
                    value = searchQuery,
                    // ^ Binds field text value to observed search query state variable
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    // ^ Runs execution callback in ViewModel when input text changes
                    modifier = Modifier.fillMaxWidth(),
                    // ^ Stretches input field fully across horizontal box width bounds
                    placeholder = { 
                    // ^ Renders layout when text field is empty
                        Text(
                        // ^ Draws placeholder text label
                            text = stringResource(R.string.search_placeholder),
                            // ^ Loads localized "Search Lectures..." label from XML resources
                            style = MaterialTheme.typography.bodyLarge,
                            // ^ Enforces 18sp minimum font size for accessibility scan compliance
                            color = Color.DarkGray
                            // ^ Colors placeholder text dark gray for secondary contrast
                        ) 
                        // ^ Ends Text placeholder widget
                    },
                    // ^ Ends placeholder block
                    leadingIcon = { 
                    // ^ Draws graphic icon inside input field start edge
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.icon_desc_search), tint = Color.DarkGray) 
                        // ^ Draws search magnifying glass icon with local screen reader label
                    },
                    // ^ Ends leadingIcon block
                    trailingIcon = {
                    // ^ Draws graphic button inside input field trailing edge
                        if (searchQuery.isNotEmpty()) {
                        // ^ Conditional check, displaying clear button only when field contains text
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            // ^ Renders button executing query reset on press
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.icon_desc_clear), tint = Color.DarkGray)
                                // ^ Draws 'X' clear icon with local screen reader label
                            }
                            // ^ Ends clear IconButton widget
                        }
                        // ^ Ends conditional check
                    },
                    // ^ Ends trailingIcon block
                    singleLine = true,
                    // ^ Restricts input field height to a single text line
                    shape = RoundedCornerShape(12.dp),
                    // ^ Rounds text field corners with 12dp radius
                    colors = OutlinedTextFieldDefaults.colors(
                    // ^ Configures color themes inside search input borders
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        // ^ Sets surface background when active/focused
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        // ^ Sets surface background when idle/unfocused
                        focusedBorderColor = Color.Transparent,
                        // ^ Hides border outline indicator when active
                        unfocusedBorderColor = Color.Transparent
                        // ^ Hides border outline indicator when idle
                    ),
                    // ^ Ends colors definition mapping
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                    // ^ Applies default 18sp text style color matching standard theme surface text
                )
                // ^ Ends OutlinedTextField composable
            }
            // ^ Ends Box container layout

            LazyColumn(
            // ^ Vertically scrollable item layout listing filtered lecture videos
                modifier = Modifier.fillMaxSize(),
                // ^ Stretches list size to occupy total available column canvas space
                contentPadding = PaddingValues(16.dp),
                // ^ Adds 16dp margins surrounding list contents inside scroll bounds
                verticalArrangement = Arrangement.spacedBy(16.dp)
                // ^ Places a neat 16dp gap space between adjacent video card list items
            ) {
            // ^ Starts scrolling list scope
                items(filteredVideos) { video ->
                // ^ Loops through lists of filtered video entries
                    VideoCard(
                    // ^ Custom list item card component representing video data
                        video = video,
                        // ^ Feeds raw video database entity metadata
                        onClick = { 
                        // ^ Executes action callback when clicked
                            println("Playing video: ${video.title}") 
                            // ^ Temporary placeholder action: prints message (Phase 6 wires ExoPlayer launch)
                        }
                        // ^ Ends onClick action listener
                    )
                    // ^ Ends VideoCard composable
                }
                // ^ Ends items loop mapping

                if (filteredVideos.isEmpty()) {
                // ^ Conditional check, displaying empty state label when no match is found
                    item {
                    // ^ Renders a single static item block representing empty state label
                        Text(
                        // ^ Draws empty search alert message label
                            text = stringResource(R.string.no_videos_found),
                            // ^ Loads localized "No Videos Found" label from XML resources
                            style = MaterialTheme.typography.bodyLarge,
                            // ^ Applies default 18sp typography size configuration
                            color = Color.Gray,
                            // ^ Colors message label gray to mark it secondary
                            modifier = Modifier.padding(top = 32.dp).fillMaxWidth(),
                            // ^ Adds 32dp top spacing and stretches text box horizontal width
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            // ^ Centers empty state text horizontally inside layout bounds
                        )
                        // ^ Ends empty alert Text widget
                    }
                    // ^ Ends empty state item block
                }
                // ^ Ends empty check block
            }
            // ^ Ends LazyColumn list container
        }
        // ^ Ends Column layout
    }
    // ^ Ends Scaffold contents block
}
// ^ Ends VideoLibraryScreen function block
