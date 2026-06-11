package com.rushdululilm.app.ui.components

// File: VideoCard.kt
// Purpose: Displays a single video entry in the video library list.
// Layer: Layer 1 — Android App (UI Component)
// Depends on: RelatedVideo.kt, Color.kt, Theme.kt
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
// ^ Modifier extension that draws background shapes and color fills
import androidx.compose.foundation.clickable
// ^ Modifier extension registering tap click callbacks
import androidx.compose.foundation.layout.*
// ^ Imports standard Compose layout widgets (Column, Row, Box, Spacer) and padding sizes
import androidx.compose.foundation.shape.RoundedCornerShape
// ^ Class representing shapes with rounded corners (used for clipping card frames)
import androidx.compose.material3.Card
// ^ Material3 container widget with borders, elevations, and rounded corners
import androidx.compose.material3.CardDefaults
// ^ Material3 class containing standard styling color defaults for Cards
import androidx.compose.material3.MaterialTheme
// ^ Component providing access to our custom colors, fonts, and shapes theme configurations
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.ui.Alignment
// ^ Alignment layout directions used to center or position items
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.draw.shadow
// ^ Modifier extension drawing dropshadow elevations beneath components
import androidx.compose.ui.graphics.Color
// ^ Compose class defining colors using ARGB Hex values
import androidx.compose.ui.text.font.FontWeight
// ^ Compose class defining text font weight thickness (e.g. Bold, Medium, Normal)
import androidx.compose.ui.unit.dp
// ^ Extension property converting numbers to density-independent pixels (dp)
import com.rushdululilm.app.model.RelatedVideo
// ^ Imports the video metadata model containing title, scholar name, and duration
import com.rushdululilm.app.ui.theme.IslamicGreen
// ^ Imports our primary branded green color swatch
import androidx.compose.ui.semantics.semantics
// ^ Modifier extension adding accessibility features (like custom text descriptions for screen readers)
import androidx.compose.ui.semantics.contentDescription
// ^ Accessibility property holding the spoken text readout representing a UI block

// 🏛️ CONCEPT: Cards are modular container widgets. We use semantics modifiers to merge all children texts 
//    into a single accessibility readout, so blind users can select the full card instead of struggling with tiny text fragments.
// 🏛️ ANALOGY: VideoCard is like a videotape cover case on a rental shelf. 
//    The cover prints the title, scholar name, and duration inside a bordered package, showing a play icon (▶) representing movie content.
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun VideoCard(
// ^ Declares VideoCard function
    video: RelatedVideo,
    // ^ parameter representing the RelatedVideo data model to display
    onClick: () -> Unit
    // ^ parameter callback invoked when the user taps this video card
) {
// ^ Starts VideoCard body
    Card(
    // ^ Draws the Material3 card container frame
        modifier = Modifier
        // ^ Instantiates modifier builder
            .fillMaxWidth()
            // ^ Expands width to match parent column width
            .shadow(8.dp, RoundedCornerShape(8.dp))
            // ^ Adds a soft dropshadow elevation of 8dp with rounded corner borders
            .clickable { onClick() }
            // ^ Registers the tap click event listener
            .semantics(mergeDescendants = true) {
            // ^ Tells accessibility services (like TalkBack) to merge all internal texts into one spoken block
                contentDescription = "Video by ${video.scholarName}, Title: ${video.title}"
                // ^ Mapped text that TalkBack will speak when a blind user highlights this card
            },
            // ^ Ends semantics block
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        // ^ Sets card container background color to our theme's surface color (BackgroundCream)
        shape = RoundedCornerShape(8.dp)
        // ^ Clips the card corners to a soft 8dp radius curve
    ) {
    // ^ Starts Card body
        Row(
        // ^ Row aligns layout items horizontally from left to right
            modifier = Modifier.padding(12.dp),
            // ^ Adds 12dp inner padding around row content
            verticalAlignment = Alignment.CenterVertically
            // ^ Centers child widgets vertically along the horizontal row line
        ) {
        // ^ Starts Row body
            Box(
            // ^ Box container acts as the mock video thumbnail image placeholder
                modifier = Modifier
                // ^ Instantiates modifier builder
                    .size(width = 80.dp, height = 60.dp)
                    // ^ Sets explicit thumbnail width of 80dp and height of 60dp
                    .background(Color.LightGray, shape = RoundedCornerShape(4.dp)),
                    // ^ Fills box with light grey background with small 4dp rounded corners
                contentAlignment = Alignment.Center
                // ^ Centers the child play icon inside the thumbnail box
            ) {
            // ^ Starts Box body
                Text(
                // ^ Draws play symbol character
                    text = "▶", 
                    // ^ Draws play arrow text character
                    color = Color.DarkGray
                    // ^ Sets symbol color to dark grey
                )
                // ^ Ends Text widget
            }
            // ^ Ends Box widget
            
            Spacer(modifier = Modifier.width(12.dp))
            // ^ Adds a fixed horizontal spacing gap of 12dp between thumbnail and text info
            
            Column(modifier = Modifier.weight(1f)) {
            // ^ Column aligns text vertically. weight(1f) expands it to take up all remaining horizontal row space.
                Text(
                // ^ Draws scholar speaker name label
                    text = video.scholarName,
                    // ^ Passes scholar name string from model
                    style = MaterialTheme.typography.labelSmall, 
                    // ^ Applies labelSmall typography style (16sp accessibility size)
                    color = IslamicGreen
                    // ^ Tints text color to IslamicGreen for branding highlight
                )
                // ^ Ends Text widget
                
                Text(
                // ^ Draws video lecture title text
                    text = video.title,
                    // ^ Passes video title string from model
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), 
                    // ^ Applies bodyLarge typography style (18sp) with bold weight styling
                    color = MaterialTheme.colorScheme.onSurface,
                    // ^ Sets text color matching high contrast onSurface theme color
                    maxLines = 2
                    // ^ Restricts display to a maximum of 2 lines, cutting off extra text with ellipses
                )
                // ^ Ends Text widget
                
                val minutes = video.durationSeconds / 60
                // ^ Computes integer division to extract total minutes value from seconds duration
                
                val seconds = video.durationSeconds % 60
                // ^ Computes modulo remainder to extract remaining seconds value
                
                Text(
                // ^ Draws formatted video duration label
                    text = String.format("%02d:%02d", minutes, seconds),
                    // ^ Formats integers into standard MM:SS clock representation
                    style = MaterialTheme.typography.labelSmall, 
                    // ^ Applies labelSmall typography style (16sp accessibility size)
                    color = Color.Gray
                    // ^ Tints text color to soft grey
                )
                // ^ Ends Text widget
            }
            // ^ Ends Column container
        }
        // ^ Ends Row container
    }
    // ^ Ends Card container
}
// ^ Ends VideoCard Composable function
