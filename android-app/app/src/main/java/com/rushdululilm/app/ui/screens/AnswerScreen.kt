package com.rushdululilm.app.ui.screens
// ^ Package declaration defining the namespace where this file lives in the application

// File: AnswerScreen.kt
// Purpose: Displays the Islamic answer, source URL, and the 'Read Aloud' button.
// Layer: Layer 1 — Android App (UI)
// Depends on: AnswerViewModel.kt, AnswerModels.kt
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import android.content.Intent
// ^ Android framework class to trigger standard device actions like opening web URLs in external browsers
import android.net.Uri
// ^ Android utility class to parse string web addresses into structured URI objects for Intent processing
import androidx.compose.foundation.background
// ^ Compose modifier function to draw a solid color overlay behind layout container elements
import androidx.compose.foundation.clickable
// ^ Compose modifier enabling touch gestures and click responsiveness on static layouts
import androidx.compose.foundation.layout.*
// ^ Imports standard Compose layouts (Column, Row, Box, Spacer) and dimensional paddings
import androidx.compose.foundation.lazy.LazyColumn
// ^ Highly efficient vertical list component that draws only items currently visible on the screen
import androidx.compose.foundation.lazy.LazyRow
// ^ Highly efficient horizontal list component that draws only items currently visible on the screen
import androidx.compose.foundation.lazy.items
// ^ Compose extension helper function to loop through Kotlin lists inside LazyColumn/LazyRow blocks
import androidx.compose.foundation.shape.RoundedCornerShape
// ^ Layout builder class used to round card corners and make buttons look like smooth pills
import androidx.compose.foundation.text.ClickableText
// ^ Composable text widget that detects clicks on specific annotated character spans (like hyperlink targets)
import androidx.compose.material.icons.Icons
// ^ Object containing the standard catalog of Material design system graphic vector icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// ^ Auto-mirrored back arrow icon that flips dynamically for right-to-left (RTL) reading layouts
import androidx.compose.material3.*
// ^ Imports all core components from Google's Material Design 3 library (TopAppBar, Button, Text, Card)
import androidx.compose.runtime.Composable
// ^ Compiler annotation signaling that this function constructs drawing operations in Compose
import androidx.compose.runtime.collectAsState
// ^ Extension mapping StateFlow dynamic streams into Compose State objects to trigger automatic UI redraws
import androidx.compose.runtime.getValue
// ^ Kotlin property delegate getter enabling direct read of Compose state values without calling .value
import androidx.compose.ui.Alignment
// ^ Layout utility class to align children within container structures (e.g. Center, TopStart)
import androidx.compose.ui.Modifier
// ^ Main builder class used to chain layout sizes, padding margins, shadow depths, and click events
import androidx.compose.ui.draw.clip
// ^ Layout modifier to crop drawing boundaries of components into rounded shapes
import androidx.compose.ui.draw.shadow
// ^ Layout modifier to project elevation drop-shadows beneath buttons and cards
import androidx.compose.ui.graphics.Color
// ^ Graphics color representation class supporting 32-bit Hex integer values
import androidx.compose.ui.platform.LocalContext
// ^ CompositionLocal variable providing access to the current active Android Context resource manager
import androidx.compose.ui.text.SpanStyle
// ^ Style configuration mapping font weight, color, and size options to text segments
import androidx.compose.ui.text.buildAnnotatedString
// ^ String builder producing rich text with customized styling applied to selective character ranges
import androidx.compose.ui.text.font.FontWeight
// ^ Type style class defining standard character weight values (like Bold, Medium, Normal)
import androidx.compose.ui.text.style.TextDecoration
// ^ Typography configurations for adding decorations like Underline or Strikethrough
import androidx.compose.ui.text.withStyle
// ^ String builder block helper that applies styling selectively inside buildAnnotatedString
import androidx.compose.ui.unit.dp
// ^ Converts standard numbers into density-independent pixels for device display scaling
import androidx.compose.ui.unit.sp
// ^ Converts standard numbers into scale-independent pixels for user font-size configurations
import androidx.compose.ui.semantics.semantics
// ^ Compose modifier to expose layout structural traits to screen readers and automated testing frameworks
import androidx.compose.ui.semantics.contentDescription
// ^ Accessibility label mapping screen reader text prompts to non-verbal interactive icons
import androidx.hilt.navigation.compose.hiltViewModel
// ^ Dagger Hilt integration function that automatically creates or loads viewmodels scoped to navigation
import androidx.navigation.NavController
// ^ Class managing navigation actions and backing histories between app screens
import androidx.compose.ui.res.stringResource
// ^ Composable helper fetching localized string texts from the values XML resources at runtime
import com.rushdululilm.app.R
// ^ Generated Android resources registry mapping references to raw string and layouts indices
import com.rushdululilm.app.model.RelatedVideo
// ^ Data structure mapping video attributes (title, scholar name, duration, and local file uri)
import com.rushdululilm.app.ui.theme.IslamicGreen
// ^ Primary design brand color representing halal states and successful QA answers
import com.rushdululilm.app.ui.theme.QuranicBlue
// ^ Custom accent design color representing Quranic references and source citation links
import com.rushdululilm.app.viewmodel.AnswerViewModel
// ^ Brain ViewModel class that processes Text-To-Speech requests and loads database fatwa answers

// 🏛️ CONCEPT: State Flow representation binds view actions to ViewModel states asynchronously.
//    The UI draws layout views depending entirely on the active State data.
// 🏛️ ANALOGY: AnswerScreen is like a digital printed scroll of a scholar's answer.
//    It shows the original question, list of official website sources, and the complete fatwa text.
//    At the bottom is a speaker assistant (Read Aloud button) and a shelf of reference lecture tapes (Related Videos).
import androidx.compose.runtime.LaunchedEffect
// ^ Side-effect API that runs suspend functions when keys change

@OptIn(ExperimentalMaterial3Api::class)
// ^ Tells compiler we are opting into Experimental Material 3 APIs (like TopAppBar configuration)
@Composable
// ^ Annotation identifying this function as a Compose layout drawing interface block
fun AnswerScreen(
// ^ Declares AnswerScreen composable entry point
    navController: NavController,
    // ^ Parameter carrying NavController for navigating back to previous screens
    answerId: String? = null,
    // ^ Optional parameter containing the specific answer ID to load from history
    answerViewModel: AnswerViewModel = hiltViewModel()
    // ^ Supplies the active AnswerViewModel instance injected by Hilt by default
) {
// ^ Starts AnswerScreen body block

    LaunchedEffect(answerId) {
    // ^ Runs a coroutine side-effect once when the screen loads with a specific answerId
        if (answerId != null) {
        // ^ Checks if an ID was passed in
            answerViewModel.loadAnswer(answerId)
            // ^ Commands the ViewModel to load the specific answer from the database
        }
    }
    // ^ Ends LaunchedEffect

    val currentAnswer by answerViewModel.currentAnswer.collectAsState()
    // ^ Binds currentAnswer state changes to refresh screen text cards dynamically
    
    val relatedVideos by answerViewModel.relatedVideos.collectAsState()
    // ^ Binds relatedVideos list state changes to refresh video row carousels dynamically
    
    val isReadingAloud by answerViewModel.isReadingAloud.collectAsState()
    // ^ Binds isReadingAloud boolean state to change button text/colors during speech playback

    val context = LocalContext.current
    // ^ Extracts the active Context reference needed for launching system Web browser intents

    Scaffold(
    // ^ Structural layout widget managing standard material screen scaffold elements
        topBar = {
        // ^ Attaches top app bar header section to the Scaffold layout
            TopAppBar(
            // ^ Displays standard Material 3 screen header bar
                title = { Text(stringResource(R.string.answer_screen_label)) },
                // ^ Binds localized screen label resource to the top bar title
                navigationIcon = {
                // ^ Attaches action icon to the left side of the top bar
                    IconButton(onClick = { navController.popBackStack() }) {
                    // ^ Renders circular button executing back-navigation pop stack on press
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.icon_desc_back))
                        // ^ Draws standard RTL-compatible back arrow icon with local screen reader label
                    }
                    // ^ Ends navigation button layout
                },
                // ^ Ends navigationIcon definition
                colors = TopAppBarDefaults.topAppBarColors(
                // ^ Configures color themes applied to header bar contents
                    containerColor = MaterialTheme.colorScheme.primary,
                    // ^ Sets background of top bar to our main primary theme color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    // ^ Sets title text color to contrast color on primary background
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    // ^ Sets back arrow icon color to contrast color on primary background
                )
                // ^ Ends color configuration mapping
            )
            // ^ Ends TopAppBar composable definition
        },
        // ^ Ends topBar configuration
        bottomBar = {
        // ^ Attaches a persistent bar to the bottom of the screen scaffold
            Surface(
            // ^ Draws elevation drawing plane behind the bottom bar
                color = MaterialTheme.colorScheme.background,
                // ^ Sets surface background matching main screen colors
                shadowElevation = 8.dp
                // ^ Projects soft shadow above the bottom bar to separate it from scroll list
            ) {
            // ^ Starts Surface contents block
                Box(modifier = Modifier.padding(16.dp)) {
                // ^ Box wrapper adding 16dp margins around bottom bar buttons
                    Button(
                    // ^ Primary interactive action button for TTS activation
                        onClick = { answerViewModel.onReadAloudPressed() },
                        // ^ Executes ViewModel Text-to-Speech function when pressed
                        modifier = Modifier
                        // ^ Chain modifiers to format the button size
                            .fillMaxWidth()
                            // ^ Forces the button to stretch fully across horizontal screen width
                            .heightIn(min = 56.dp),
                            // ^ Enforces 56dp minimum height for easy touch accessibility targets
                        colors = ButtonDefaults.buttonColors(
                        // ^ Customizes button state backgrounds based on active execution
                            containerColor = if (isReadingAloud) Color.Gray else IslamicGreen
                            // ^ Shows passive IslamicGreen when idle, changes to gray when reading aloud
                        )
                        // ^ Ends colors definition mapping
                    ) {
                    // ^ Starts Button contents layout
                        Text(
                        // ^ Renders text label inside the button area
                            text = if (isReadingAloud) stringResource(R.string.stop_reading_button) else stringResource(R.string.read_aloud_button),
                            // ^ Swaps between Read Aloud and Stop Reading labels dynamically
                            style = MaterialTheme.typography.bodyLarge
                            // ^ Enforces readable 18sp text style for accessibility compliance
                        )
                        // ^ Ends Text widget configuration
                    }
                    // ^ Ends Button composable
                }
                // ^ Ends Box container layout
            }
            // ^ Ends Surface elevation plane
        }
        // ^ Ends bottomBar layout block
    ) { innerPadding ->
    // ^ Exposes padding spacing calculations to prevent content overlap with headers/footers
        LazyColumn(
        // ^ Vertically scrollable item layout mapping only visible components to conserve RAM
            modifier = Modifier
            // ^ Chain modifiers to style the main scrolling list canvas
                .fillMaxSize()
                // ^ Stretches list size to occupy total available scaffold window space
                .padding(innerPadding)
                // ^ Subtracts header/footer bar height margins from active canvas area
                .background(MaterialTheme.colorScheme.background),
                // ^ Draws background color matches behind scrolling items
            contentPadding = PaddingValues(16.dp)
            // ^ Adds 16dp inner margins surrounding list contents inside the scroll bounds
        ) {
        // ^ Starts LazyColumn scope
            currentAnswer?.let { answer ->
            // ^ Safe let-guard block, executing UI construction only if answer content is loaded
                
                item {
                // ^ Defines a single static item block inside the vertical scroll list
                    Text(
                    // ^ Renders header text for original user question section
                        text = stringResource(R.string.your_question_label),
                        // ^ Loads question title label from local string resources
                        style = MaterialTheme.typography.labelSmall,
                        // ^ Sets default 16sp label style configurations
                        color = IslamicGreen,
                        // ^ Marks title color green to stand out
                        fontWeight = FontWeight.Bold
                        // ^ Emphasizes label using bold character thickness
                    )
                    // ^ Ends label Text widget
                    
                    Text(
                    // ^ Renders user's transcribed question text
                        text = answer.questionText,
                        // ^ Pulls question text directly from active data model
                        style = MaterialTheme.typography.bodyMedium,
                        // ^ Formats body text to standard readable 16sp sizes
                        color = MaterialTheme.colorScheme.onBackground,
                        // ^ Adapts contrast color compatible with background theme
                        modifier = Modifier.padding(bottom = 16.dp)
                        // ^ Appends 16dp bottom spacing to separate question from sources
                    )
                    // ^ Ends question content Text widget

                    Row(
                    // ^ Horizontally aligns source badge chips
                        modifier = Modifier.fillMaxWidth(),
                        // ^ Stretches Row container to fill horizontal card width
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                        // ^ Places a neat 8dp gap space between adjacent badge chips
                    ) {
                    // ^ Starts horizontal badges layout
                        answer.sources.forEach { source ->
                        // ^ Loops through every citation source database reference
                            Box(
                            // ^ Badge shape container box
                                modifier = Modifier
                                // ^ Formats chip background outline style
                                    .clip(RoundedCornerShape(16.dp))
                                    // ^ Rounds box borders into a smooth modern pill shape
                                    .background(QuranicBlue)
                                    // ^ Colors badge background blue to signal citation reference
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    // ^ Adds text padding inside pill borders
                            ) {
                            // ^ Starts badge contents container
                                Text(
                                // ^ Draws name text of source site
                                    text = source.name,
                                    // ^ Pulls website name string (e.g. IslamQA.info)
                                    color = Color.White,
                                    // ^ Sets text color to white for contrast on blue background
                                    style = MaterialTheme.typography.labelSmall
                                    // ^ Applies clean 16sp typography configuration
                                )
                                // ^ Ends badge Text
                            }
                            // ^ Ends badge Box shape
                        }
                        // ^ Ends loop
                    }
                    // ^ Ends horizontal badges Row

                    Spacer(modifier = Modifier.height(16.dp))
                    // ^ Places 16dp blank spacer layout to separate badges from answer content

                    Text(
                    // ^ Draws main answer text block body
                        text = answer.answerText,
                        // ^ Feeds raw scholarly answer string fetched from vector database
                        style = MaterialTheme.typography.bodyLarge,
                        // ^ Applies prominent 18sp minimum font size for readability
                        color = MaterialTheme.colorScheme.onBackground,
                        // ^ Sets high-contrast color for easy text scanning
                        lineHeight = 28.sp
                        // ^ Configures generous 28sp vertical line height to help readers follow sentences
                    )
                    // ^ Ends main answer Text widget

                    Spacer(modifier = Modifier.height(24.dp))
                    // ^ Adds 24dp blank gap before showing AI transparency logs

                    answer.expandedQuery?.let { query ->
                    // ^ Safe let block, rendering AI search logs only if available
                        Card(
                        // ^ Renders card layout summarizing local RAG semantic lookup details
                            modifier = Modifier
                            // ^ Modifier configurations for card layout
                                .fillMaxWidth()
                                // ^ Stretches card fully across screen content width
                                .padding(vertical = 8.dp),
                                // ^ Adds 8dp vertical margins surrounding the card
                            colors = CardDefaults.cardColors(
                            // ^ Sets card container background styles
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                // ^ Applies semi-transparent background tone to show technical context
                            ),
                            // ^ Ends colors parameter
                            shape = RoundedCornerShape(12.dp)
                            // ^ Rounds card corners with 12dp radius
                        ) {
                        // ^ Starts Card layout body content
                            Column(modifier = Modifier.padding(12.dp)) {
                            // ^ Arranges elements inside card vertically with 12dp margins
                                Text(
                                // ^ Draws title header for AI search card
                                    text = stringResource(R.string.ai_search_logic_label),
                                    // ^ Fetches "AI Search Query Details" from XML strings
                                    style = MaterialTheme.typography.labelSmall,
                                    // ^ Applies standard 16sp label typography
                                    fontWeight = FontWeight.Bold,
                                    // ^ Emphasizes title using bold weight
                                    color = QuranicBlue
                                    // ^ Highlights title with blue color
                                )
                                // ^ Ends card title Text
                                
                                Text(
                                // ^ Draws short description of the RAG lookup expansion mechanism
                                    text = stringResource(R.string.ai_search_logic_desc),
                                    // ^ Loads RAG search process explanations from string XML resources
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                                    // ^ Adapts small 14sp explanation text size to fit secondary info
                                    color = Color.Gray,
                                    // ^ Sets color of explanation text to neutral gray
                                    modifier = Modifier.padding(vertical = 4.dp)
                                    // ^ Adds 4dp vertical margins around the description
                                )
                                // ^ Ends card description Text
                                
                                Text(
                                // ^ Draws the expanded query string used by Ollama/Qdrant lookup
                                    text = "\"$query\"",
                                    // ^ Wraps technical query inside quotes to mark it clearly
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    // ^ Sets medium weight 16sp text style
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                    // ^ Applies theme-neutral surface contrast color
                                )
                                // ^ Ends query string Text
                            }
                            // ^ Ends card Column layout
                        }
                        // ^ Ends Card container widget
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        // ^ Adds 16dp spacer height block below the card
                    }
                    // ^ Ends query let block

                    answer.sources.forEach { source ->
                    // ^ Loops through citation sources list to output clickable reference links
                        Row(
                        // ^ Horizontal arrangement aligning website label and link text
                            verticalAlignment = Alignment.CenterVertically,
                            // ^ Aligns label and hyperlink texts along horizontal centerline
                            modifier = Modifier.padding(vertical = 4.dp)
                            // ^ Adds 4dp vertical margin spacing to separate citation rows
                        ) {
                        // ^ Starts citation Row block
                            Text(
                            // ^ Renders static label showing source provider name
                                text = "${source.name}: ",
                                // ^ Formats source name prefix label (e.g. IslamQA.info:)
                                style = MaterialTheme.typography.bodyMedium,
                                // ^ Applies 16sp default body typography
                                color = Color.Gray
                                // ^ Colors prefix text gray to highlight the clickable link next to it
                            )
                            // ^ Ends prefix Text
                            
                            val annotatedLinkString = buildAnnotatedString {
                            // ^ Builds custom string with hyperlink properties
                                val viewOriginal = stringResource(R.string.view_original_link)
                                // ^ Pulls localized "View Original" link text label
                                withStyle(
                                // ^ Opens styled block mapping details to link characters
                                    style = SpanStyle(
                                    // ^ Configures hyperlink appearance details
                                        color = QuranicBlue,
                                        // ^ Colors link text blue
                                        textDecoration = TextDecoration.Underline,
                                        // ^ Appends underline decoration style
                                        fontSize = 16.sp
                                        // ^ Enforces 16sp minimum size for readability compliance
                                    )
                                    // ^ Ends SpanStyle mapping
                                ) {
                                // ^ Starts style application block
                                    append(viewOriginal)
                                    // ^ Appends "View Original" string to styled region
                                }
                                // ^ Ends style application block
                                
                                addStringAnnotation(
                                // ^ Appends raw URL metadata properties to the text span
                                    tag = "URL",
                                    // ^ Identifies metadata type tag as URL
                                    annotation = source.url,
                                    // ^ Attaches citation URL address string
                                    start = 0,
                                    // ^ Sets target start character position
                                    end = viewOriginal.length
                                    // ^ Sets target end character position
                                )
                                // ^ Ends annotation mapping
                            }
                            // ^ Ends buildAnnotatedString block

                            ClickableText(
                            // ^ Renders interactive hyperlink text widget
                                text = annotatedLinkString,
                                // ^ Sets rich annotated link string contents
                                onClick = { offset ->
                                // ^ Runs execution block capturing click index details
                                    annotatedLinkString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    // ^ Searches for URL tags intersecting user's touch coordinate
                                        .firstOrNull()?.let { annotation ->
                                        // ^ Checks if valid URL metadata is found under click location
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                            // ^ Instantiates system action Intent targeting browser URL opening
                                            context.startActivity(intent)
                                            // ^ Triggers system intent activity, redirecting user to browser
                                        }
                                        // ^ Ends let block
                                }
                                // ^ Ends onClick execution listener
                            )
                            // ^ Ends ClickableText composable
                        }
                        // ^ Ends citation Row
                    }
                    // ^ Ends loop

                    Spacer(modifier = Modifier.height(16.dp))
                    // ^ Adds 16dp spacing height
                    
                    HorizontalDivider()
                    // ^ Draws thin material dividing line separator
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    // ^ Adds 16dp spacing height

                    Text(
                    // ^ Draws header title for related video suggestions
                        text = stringResource(R.string.related_lectures_label),
                        // ^ Loads "Related Video Lectures" title from XML strings
                        style = MaterialTheme.typography.titleLarge,
                        // ^ Applies 22sp bold header typography configurations
                        color = MaterialTheme.colorScheme.onBackground
                        // ^ Sets high-contrast color matching active theme
                    )
                    // ^ Ends related lectures title Text

                    Spacer(modifier = Modifier.height(12.dp))
                    // ^ Adds 12dp blank gap spacing before list carousel
                }
                // ^ Ends main answer page item content block

                item {
                // ^ Defines horizontal scrolling carousel item block in LazyColumn
                    LazyRow(
                    // ^ Horizontally scrollable row containing video lecture suggestions
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        // ^ Formats 16dp gap spacing between adjacent video cards
                        modifier = Modifier.fillMaxWidth()
                        // ^ Stretches carousel layout across horizontal screen width
                    ) {
                    // ^ Starts horizontal list contents mapping
                        items(relatedVideos) { video ->
                        // ^ Loops through lists of related video items
                            AnswerVideoCard(video = video, onPlayClick = { answerViewModel.onVideoClicked(video) })
                            // ^ Instantiates customized video card layouts binding click listeners
                        }
                        // ^ Ends carousel loop
                    }
                    // ^ Ends LazyRow list container
                }
                // ^ Ends carousel item block
            }
            // ^ Ends guard check let block
        }
        // ^ Ends vertical LazyColumn list container
    }
    // ^ Ends Scaffold contents block
}
// ^ Ends AnswerScreen function block

// 🏛️ CONCEPT: Reusable UI cards isolate UI widget styles from parent page frameworks.
//    Making layout sub-elements separate functions increases readability and testability.
// 🏛️ ANALOGY: AnswerVideoCard is like a small display box containing a video tape.
//    It labels the scholar (teacher), title (lecture topic), duration (length), and a play button to insert it into the player.
@Composable
// ^ Annotation signaling this function draws UI widgets in Compose
fun AnswerVideoCard(video: RelatedVideo, onPlayClick: () -> Unit) {
// ^ Declares custom AnswerVideoCard composable, accepting video details and click actions
    Card(
    // ^ Draws Material 3 card container
        modifier = Modifier
        // ^ Formats card constraints and accessibility details
            .width(280.dp)
            // ^ Locks horizontal card width to 280dp for horizontal carousels
            .shadow(4.dp, RoundedCornerShape(8.dp))
            // ^ Projects card depth shadow with 8dp rounded corner shapes
            .semantics(mergeDescendants = true) {
            // ^ Tells accessibility system to merge sub-text components into single screen reader voice prompts
                contentDescription = "Related Video by ${video.scholarName}, Title: ${video.title}"
                // ^ Specifies descriptive spoken prompt summarizing video properties
            },
            // ^ Ends semantics definition
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        // ^ Sets card background color matching default theme surface
    ) {
    // ^ Starts Card contents layout
        Column(modifier = Modifier.padding(12.dp)) {
        // ^ Arranges video details vertically with 12dp inner padding bounds
            Text(
            // ^ Renders scholar name text label
                text = video.scholarName,
                // ^ Pulls scholar name string from video object
                style = MaterialTheme.typography.labelSmall,
                // ^ Applies 16sp label typography specifications
                color = IslamicGreen
                // ^ Highlights scholar name green
            )
            // ^ Ends scholar Text
            
            Spacer(modifier = Modifier.height(4.dp))
            // ^ Adds 4dp blank gap spacing
            
            Text(
            // ^ Renders video lecture title text
                text = video.title,
                // ^ Pulls title string from video object
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                // ^ Applies bold 18sp body typography
                color = MaterialTheme.colorScheme.onSurface,
                // ^ Sets high-contrast color matching theme surfaces
                maxLines = 2
                // ^ Limits title height to 2 lines maximum to protect card layouts from text overflow
            )
            // ^ Ends title Text
            
            Spacer(modifier = Modifier.height(4.dp))
            // ^ Adds 4dp blank gap spacing
            
            val minutes = video.durationSeconds / 60
            // ^ Calculates minutes count by dividing duration seconds by 60
            
            val seconds = video.durationSeconds % 60
            // ^ Calculates remaining seconds count using remainder modulo 60
            
            Text(
            // ^ Renders formatted video duration label
                text = String.format("%02d:%02d", minutes, seconds),
                // ^ Formats MM:SS duration display string
                style = MaterialTheme.typography.bodyMedium,
                // ^ Applies default 16sp body typography
                color = Color.Gray
                // ^ Colors duration text gray for secondary contrast
            )
            // ^ Ends duration Text
            
            Spacer(modifier = Modifier.height(8.dp))
            // ^ Adds 8dp blank gap space
            
            Button(
            // ^ Renders video playback activation button
                onClick = onPlayClick,
                // ^ Executes play callback function passed by parent screen
                modifier = Modifier.fillMaxWidth(),
                // ^ Stretches play button to fill container card width
                colors = ButtonDefaults.buttonColors(containerColor = QuranicBlue)
                // ^ Colors button background QuranicBlue
            ) {
            // ^ Starts play Button content block
                Text(stringResource(R.string.play_button), style = MaterialTheme.typography.bodyMedium)
                // ^ Loads localized "Play" text resource using 16sp font configurations
            }
            // ^ Ends Button
        }
        // ^ Ends Column layout
    }
    // ^ Ends Card container
}
// ^ Ends AnswerVideoCard function block
