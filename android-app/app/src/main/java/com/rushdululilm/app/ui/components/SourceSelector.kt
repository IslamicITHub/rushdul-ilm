package com.rushdululilm.app.ui.components

// File: SourceSelector.kt
// Purpose: Allows the user to pick which Islamic database (fatwa source/Madhab) to query.
// Layer: Layer 1 — Android App (UI Component)
// Depends on: strings.xml, Color.kt, Theme.kt, R
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.layout.Column
// ^ Compose vertical layout container
import androidx.compose.foundation.layout.Spacer
// ^ Composable space filler used to separate layouts
import androidx.compose.foundation.layout.fillMaxWidth
// ^ Modifier extension expanding width to match parent layout size
import androidx.compose.foundation.layout.height
// ^ Modifier extension setting layout height
import androidx.compose.foundation.layout.padding
// ^ Modifier extension adding blank margins around components
import androidx.compose.foundation.lazy.LazyRow
// ^ Compose horizontally scrollable container that builds list items dynamically when visible on screen
import androidx.compose.foundation.lazy.items
// ^ Lazy layout extension that populates dynamic lists from collection data types (like List)
import androidx.compose.material3.ExperimentalMaterial3Api
// ^ Opt-in annotation for using under-development Material3 library classes
import androidx.compose.material3.FilterChip
// ^ Material3 pill-shaped toggle chip button widget used to represent selections
import androidx.compose.material3.FilterChipDefaults
// ^ Material3 class containing default styling configurations for FilterChips
import androidx.compose.material3.MaterialTheme
// ^ Component providing access to our custom colors, fonts, and shapes theme configurations
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.graphics.Color
// ^ Compose class defining colors using ARGB Hex values
import androidx.compose.ui.tooling.preview.Preview
// ^ Annotation marking parameterless Composable functions to render inside Android Studio preview
import androidx.compose.ui.unit.dp
// ^ Extension property converting numbers to density-independent pixels (dp)
import androidx.compose.ui.res.stringResource
// ^ Compose utility function to load localized string texts from strings.xml at runtime
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry
import com.rushdululilm.app.ui.theme.RushdulIlmTheme
// ^ Imports our custom theme wrapper function

// 🏛️ CONCEPT: LazyRow implements a horizontally scrolling layout list. 
//    Unlike a Row that constructs all its items at once, LazyRow is highly optimized, building chips only when visible.
// 🏛️ ANALOGY: SourceSelector is like choosing a radio presets panel. 
//    You have a row of buttons (chips: All, Hanafi, Neutral). Pressing one lights it up (turns green) and tunes the search engine to those sources.
@OptIn(ExperimentalMaterial3Api::class)
// ^ Opts in to using Experimental Material3 FilterChip APIs
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun SourceSelector(
// ^ Declares SourceSelector function
    selectedSource: String,
    // ^ parameter representing the active selection key (e.g. "hanafi", "neutral")
    onSourceSelected: (String) -> Unit
    // ^ parameter callback invoked when a chip is tapped
) {
// ^ Starts SourceSelector body
    val sources = listOf(
    // ^ Instantiates a read-only list of key-value pairs mapping stable keys to localized display strings
        "all" to stringResource(R.string.source_all),
        // ^ Maps key "all" to the "All Sources" display string
        "neutral" to stringResource(R.string.madhab_neutral),
        // ^ Maps key "neutral" to the "Neutral (No Madhab)" display string
        "hanafi" to stringResource(R.string.madhab_hanafi)
        // ^ Maps key "hanafi" to the "Hanafi (Deoband)" display string
    )
    // ^ Ends sources list configuration

    Column(modifier = Modifier.fillMaxWidth()) {
    // ^ Vertical container matching parent width
        Text(
        // ^ Draws selector section label header
            text = stringResource(R.string.source_selector_label),
            // ^ Resolves bilingual label from strings.xml
            style = MaterialTheme.typography.bodyLarge, 
            // ^ Applies bodyLarge typography style (18sp accessibility size)
            color = MaterialTheme.colorScheme.onBackground,
            // ^ Sets text color matching background theme text
            modifier = Modifier.padding(horizontal = 16.dp)
            // ^ Adds 16dp horizontal margin padding
        )
        // ^ Ends Text widget
        
        Spacer(modifier = Modifier.height(8.dp))
        // ^ Adds a fixed blank spacing gap of 8dp below the label header

        LazyRow(
        // ^ Horizontal scrollable list container
            modifier = Modifier.fillMaxWidth(),
            // ^ Expands width to match screen width bounds
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            // ^ Adds 16dp starting and ending scroll margin offsets so items don't touch screen edges
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            // ^ Configures 8dp horizontal space gaps between chips
        ) {
        // ^ Starts LazyRow body
            items(sources) { (sourceId, sourceName) ->
            // ^ Iterates through key-value pairs, extracting key (sourceId) and label (sourceName)
                FilterChip(
                // ^ Draws Material3 selectable pill-shaped button
                    selected = selectedSource == sourceId,
                    // ^ Marks chip selected if its key matches active selection
                    onClick = { onSourceSelected(sourceId) },
                    // ^ Invokes ViewModel callback with the clicked chip key
                    label = { 
                    // ^ Passes chip label widget block
                        Text(
                        // ^ Draws display name inside the chip
                            text = sourceName,
                            // ^ Passes the display name string
                            style = MaterialTheme.typography.labelSmall
                            // ^ Applies labelSmall typography style (16sp accessibility size)
                        ) 
                        // ^ Ends Text widget
                    },
                    // ^ Ends label parameter
                    colors = FilterChipDefaults.filterChipColors(
                    // ^ Customizes color schemes for active and inactive states
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        // ^ Sets selected background fill to primary theme color (IslamicGreen)
                        selectedLabelColor = Color.White
                        // ^ Sets selected text color to White
                    )
                    // ^ Ends colors parameter
                )
                // ^ Ends FilterChip widget
            }
            // ^ Ends items loop block
        }
        // ^ Ends LazyRow container
    }
    // ^ Ends Column container
}
// ^ Ends SourceSelector Composable function

@Preview(showBackground = true)
// ^ Preview configuration displaying the source selector on a standard white background in Android Studio
@Composable
// ^ Composable annotation
fun SourceSelectorPreview() {
// ^ Declares SourceSelectorPreview function
    RushdulIlmTheme {
    // ^ Wraps in custom theme colors
        SourceSelector(
            selectedSource = "all",
            // ^ Sets selection preview to "all"
            onSourceSelected = {}
            // ^ Empty callback
        )
    }
    // ^ Ends theme wrap
}
// ^ Ends SourceSelectorPreview function
