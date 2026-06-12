// File: app/src/main/java/com/rushdululilm/app/ui/screens/AnswersHistoryScreen.kt
// Purpose: Displays a list of all previously saved answers
// Layer: Layer 1 — Android App (UI Layer)
// Depends on: AnswersHistoryViewModel.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS THIS SCREEN?
 * This is the new "Answers" tab. It shows a scrollable list of past questions and answers.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/ui/screens/
 */

package com.rushdululilm.app.ui.screens
// ^ Package declaration

import androidx.compose.foundation.clickable
// ^ Imports clickable to make rows tappable
import androidx.compose.foundation.layout.*
// ^ Imports layout tools like Column, Row, Spacer, etc
import androidx.compose.foundation.lazy.LazyColumn
// ^ Imports LazyColumn for efficient scrolling lists
import androidx.compose.foundation.lazy.items
// ^ Imports items to build the list
import androidx.compose.material3.*
// ^ Imports Material 3 UI components (Text, Card, Scaffold, etc)
import androidx.compose.runtime.Composable
// ^ Imports Composable annotation
import androidx.compose.runtime.collectAsState
// ^ Imports collectAsState to read ViewModel data
import androidx.compose.runtime.getValue
// ^ Imports getValue for state reading
import androidx.compose.ui.Alignment
// ^ Imports Alignment for positioning
import androidx.compose.ui.Modifier
// ^ Imports Modifier to tweak UI element properties
import androidx.compose.ui.text.style.TextOverflow
// ^ Imports TextOverflow to cut off long text with dots
import androidx.compose.ui.unit.dp
// ^ Imports dp (density-independent pixels) for sizing
import androidx.hilt.navigation.compose.hiltViewModel
// ^ Imports hiltViewModel to connect to our ViewModel
import androidx.navigation.NavController
// ^ Imports NavController to allow moving between screens
import com.rushdululilm.app.data.local.SavedAnswer
// ^ Imports the SavedAnswer data model
import com.rushdululilm.app.viewmodel.AnswersHistoryViewModel
// ^ Imports the ViewModel
import java.text.SimpleDateFormat
// ^ Imports SimpleDateFormat to format dates
import java.util.Date
// ^ Imports Date for timestamps
import java.util.Locale
// ^ Imports Locale for date formatting

@OptIn(ExperimentalMaterial3Api::class)
// ^ Opts into ExperimentalMaterial3Api for TopAppBar usage
@Composable
// ^ Marks this as a Jetpack Compose UI function
fun AnswersHistoryScreen(
// ^ Defines the screen function
    navController: NavController,
    // ^ Accepts the navigation controller
    viewModel: AnswersHistoryViewModel = hiltViewModel()
    // ^ Asks Hilt to provide the AnswersHistoryViewModel
) {
// ^ Opens function body
    val answers by viewModel.savedAnswers.collectAsState()
    // ^ Reads the live stream of answers from the ViewModel. The UI will redraw if this changes.

    Scaffold(
    // ^ Creates the basic screen structure with a top bar
        topBar = {
        // ^ Defines the top bar section
            TopAppBar(
            // ^ Creates the Material 3 TopAppBar
                title = { Text("My Answers / నా జవాబులు") }
                // ^ Sets the bilingual title text
            )
            // ^ Closes TopAppBar
        }
        // ^ Closes topBar section
    ) { paddingValues ->
    // ^ The Scaffold gives us paddingValues so our content doesn't hide behind the top bar

        if (answers.isEmpty()) {
        // ^ Checks if the database has no answers
            Box(
            // ^ Creates a container box
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                // ^ Fills the screen and respects the top bar padding
                contentAlignment = Alignment.Center
                // ^ Centers the text inside the box
            ) {
            // ^ Opens Box body
                Text(
                // ^ Creates the text element
                    text = "📭 No saved answers yet.\nAsk your first question!",
                    // ^ Sets the empty state message
                    style = MaterialTheme.typography.bodyLarge
                    // ^ Uses bodyLarge typography (16sp minimum)
                )
                // ^ Closes Text
            }
            // ^ Closes Box
        } else {
        // ^ If there are answers in the list
            LazyColumn(
            // ^ Creates a highly efficient scrolling list that only draws visible items
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                // ^ Fills screen, adds padding for the top bar, and adds side margins
                verticalArrangement = Arrangement.spacedBy(12.dp)
                // ^ Adds 12dp spacing between each answer card
            ) {
            // ^ Opens LazyColumn body
                items(answers) { answer ->
                // ^ Loops through every SavedAnswer in our list
                    SavedAnswerCard(
                    // ^ Calls our custom card component to draw this specific answer
                        savedAnswer = answer,
                        // ^ Passes the answer data to the card
                        onClick = {
                        // ^ Defines what happens when the card is clicked
                            navController.navigate("${Routes.ANSWER}?answerId=${answer.id}")
                            // ^ Navigates to the Answer Screen and passes the specific answer ID in the URL
                        }
                        // ^ Closes onClick block
                    )
                    // ^ Closes SavedAnswerCard
                }
                // ^ Closes items loop
            }
            // ^ Closes LazyColumn
        }
        // ^ Closes if/else
    }
    // ^ Closes Scaffold
}
// ^ Closes AnswersHistoryScreen function

@Composable
// ^ Marks this as a UI component
fun SavedAnswerCard(savedAnswer: SavedAnswer, onClick: () -> Unit) {
// ^ Defines the card component that takes the data and a click action
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    // ^ Creates a date formatter to make the Unix timestamp readable (e.g. "Jun 12, 2026")
    val dateString = dateFormat.format(Date(savedAnswer.savedAtTimestamp))
    // ^ Converts the saved timestamp into a readable text string

    Card(
    // ^ Creates a Material 3 Card container
        modifier = Modifier
            .fillMaxWidth()
            // ^ Makes the card span the entire width of the screen
            .clickable(onClick = onClick),
            // ^ Makes the card tappable and triggers the onClick function (which navigates)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        // ^ Gives the card a slightly distinct background color from the main screen
    ) {
    // ^ Opens Card body
        Column(modifier = Modifier.padding(16.dp)) {
        // ^ Creates a vertical layout inside the card with 16dp padding on all sides
            Text(
            // ^ Displays the question text
                text = savedAnswer.questionText,
                // ^ Uses the saved question
                style = MaterialTheme.typography.titleMedium,
                // ^ Uses the titleMedium typography for emphasis
                maxLines = 2,
                // ^ Limits the question to 2 lines so the card doesn't get too tall
                overflow = TextOverflow.Ellipsis
                // ^ Adds "..." if the question text is too long for 2 lines
            )
            // ^ Closes Text
            
            Spacer(modifier = Modifier.height(8.dp))
            // ^ Adds 8dp of empty vertical space
            
            Row(verticalAlignment = Alignment.CenterVertically) {
            // ^ Creates a horizontal layout for the bottom details (Date | Language)
                Text(
                // ^ Displays the formatted date
                    text = dateString,
                    // ^ Uses the converted date string
                    style = MaterialTheme.typography.bodySmall,
                    // ^ Uses smaller typography for metadata
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                    // ^ Uses a muted text color
                )
                // ^ Closes Text
                
                Text(
                // ^ Displays a separator
                    text = " | ",
                    // ^ Uses a pipe symbol
                    style = MaterialTheme.typography.bodySmall,
                    // ^ Matches metadata typography
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                    // ^ Uses muted color
                )
                // ^ Closes Text
                
                Text(
                // ^ Displays the language
                    text = savedAnswer.language,
                    // ^ Uses the saved language
                    style = MaterialTheme.typography.bodySmall,
                    // ^ Matches metadata typography
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                    // ^ Uses muted color
                )
                // ^ Closes Text
            }
            // ^ Closes Row
        }
        // ^ Closes Column
    }
    // ^ Closes Card
}
// ^ Closes SavedAnswerCard
