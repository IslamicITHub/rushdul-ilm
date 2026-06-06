package com.rushdululilm.app.ui.screens

// File: AnswerScreen.kt
// Purpose: Displays the Islamic answer, source URL, and the 'Read Aloud' button.
// Layer: Layer 1 — Android App (UI)
// Depends on: AnswerViewModel.kt, AnswerModels.kt
// Created: 2026-05-30 | Modified: 2026-05-31
// Developer: Shaik Hidayatullah

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.rushdululilm.app.R
import com.rushdululilm.app.model.RelatedVideo
import com.rushdululilm.app.ui.theme.IslamicGreen
import com.rushdululilm.app.ui.theme.QuranicBlue
import com.rushdululilm.app.viewmodel.AnswerViewModel

/**
 * AnswerScreen is the view that displays the fatwa result.
 * It uses a LazyColumn so the user can scroll through long answers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(
    navController: NavController,
    answerViewModel: AnswerViewModel = hiltViewModel() // Hilt provides the ViewModel
) {
    // Observe state from ViewModel
    val currentAnswer by answerViewModel.currentAnswer.collectAsState()
    val relatedVideos by answerViewModel.relatedVideos.collectAsState()
    val isReadingAloud by answerViewModel.isReadingAloud.collectAsState()

    // LocalContext is needed to start intents (like opening a web browser)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.answer_screen_label)) },
                // Back button to return to the Home Screen
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.icon_desc_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // Read Aloud Button - MUST be prominent and easily accessible without scrolling
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { answerViewModel.onReadAloudPressed() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp), // Large touch target
                        // Change color if it is currently reading
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReadingAloud) Color.Gray else IslamicGreen
                        )
                    ) {
                        Text(
                            text = if (isReadingAloud) stringResource(R.string.stop_reading_button) else stringResource(R.string.read_aloud_button),
                            style = MaterialTheme.typography.bodyLarge // 18sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // LazyColumn is used because answers might be longer than the screen.
        // It only draws the content that is currently visible, saving memory.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp) // Add padding around all content inside the scroll
        ) {
            // We only show content if we actually have an answer to show
            currentAnswer?.let { answer ->
                
                item {
                    // Source Name Badge (e.g., IslamQA.info)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp)) // Pill shape
                            .background(QuranicBlue)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = answer.sourceName,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall // 16sp minimum
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // The actual answer text. It should be large and readable.
                    Text(
                        text = answer.answerText,
                        style = MaterialTheme.typography.bodyLarge, // 18sp minimum
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 28.sp // Extra line height for readability
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Clickable Source URL Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "మూలం / Source: ",
                            style = MaterialTheme.typography.bodyMedium, // 16sp
                            color = Color.Gray
                        )
                        
                        // We build an "Annotated String" to make part of the text look like a clickable link
                        val annotatedLinkString = buildAnnotatedString {
                            val viewOriginal = stringResource(R.string.view_original_link)
                            withStyle(
                                style = SpanStyle(
                                    color = QuranicBlue,
                                    textDecoration = TextDecoration.Underline,
                                    fontSize = 16.sp
                                )
                            ) {
                                append(viewOriginal)
                            }
                            // Attach the URL to this text segment
                            addStringAnnotation(
                                tag = "URL",
                                annotation = answer.sourceUrl,
                                start = 0,
                                end = viewOriginal.length // Length of "View Original"
                            )
                        }

                        // ClickableText handles the tap event on the annotated string
                        ClickableText(
                            text = annotatedLinkString,
                            onClick = { offset ->
                                annotatedLinkString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        // Open the browser with the URL
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider() // A thin line separating sections
                    Spacer(modifier = Modifier.height(16.dp))

                    // Related Videos Section Header
                    Text(
                        text = stringResource(R.string.related_lectures_label),
                        style = MaterialTheme.typography.titleLarge, // 22sp bold
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Horizontal scrollable list for related videos
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between cards
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(relatedVideos) { video ->
                            AnswerVideoCard(video = video, onPlayClick = { answerViewModel.onVideoClicked(video) })
                        }
                    }
                }
            }
        }
    }
}

/**
 * A reusable component to display a single video suggestion.
 */
@Composable
fun AnswerVideoCard(video: RelatedVideo, onPlayClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp) // Fixed width so they look like horizontal cards
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Related Video by ${video.scholarName}, Title: ${video.title}"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Scholar Name
            Text(
                text = video.scholarName,
                style = MaterialTheme.typography.labelSmall, // 16sp
                color = IslamicGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Video Title
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), // 18sp
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2 // Prevent extremely long titles from breaking the layout
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Format duration from seconds to MM:SS
            val minutes = video.durationSeconds / 60
            val seconds = video.durationSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.bodyMedium, // 16sp
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = QuranicBlue)
            ) {
                Text(stringResource(R.string.play_button), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
