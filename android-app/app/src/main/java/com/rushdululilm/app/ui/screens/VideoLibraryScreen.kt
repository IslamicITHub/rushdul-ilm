package com.rushdululilm.app.ui.screens

// File: VideoLibraryScreen.kt
// Purpose: Displays a list of offline Islamic video lectures that the user can play.
// Layer: Layer 1 — Android App (UI)
// Depends on: VideoLibraryViewModel.kt, VideoCard.kt
// Created: 2026-05-30 | Modified: 2026-05-31
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rushdululilm.app.ui.components.VideoCard
import com.rushdululilm.app.viewmodel.VideoLibraryViewModel

/**
 * The Video Library Screen allows the user to browse and search for offline video lectures.
 * It uses a LazyColumn for efficient scrolling of long lists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLibraryScreen(
    navController: NavController,
    viewModel: VideoLibraryViewModel = hiltViewModel() // Hilt automatically provides the ViewModel
) {
    // Observe state from the ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredVideos by viewModel.filteredVideos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "వీడియో లైబ్రరీ / Video Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            text = "అంశం లేదా పండితుడిని వెతకండి... / Search...",
                            style = MaterialTheme.typography.bodyLarge, // 18sp minimum for readability
                            color = Color.DarkGray
                        ) 
                    },
                    leadingIcon = { 
                        Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color.DarkGray) 
                    },
                    trailingIcon = {
                        // Only show the clear button (X) if there is text in the search bar
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = Color.DarkGray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            // Scrollable List of Videos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp), // Padding around the edges of the list
                verticalArrangement = Arrangement.spacedBy(16.dp) // Space between each card
            ) {
                items(filteredVideos) { video ->
                    VideoCard(
                        video = video,
                        onClick = { 
                            // Placeholder action: In Phase 6, this will open the ExoPlayer screen.
                            println("Playing video: ${video.title}") 
                        }
                    )
                }

                // If no videos match the search, show a helpful message
                if (filteredVideos.isEmpty()) {
                    item {
                        Text(
                            text = "ఎలాంటి వీడియోలు కనుగొనబడలేదు.\nNo videos found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 32.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
