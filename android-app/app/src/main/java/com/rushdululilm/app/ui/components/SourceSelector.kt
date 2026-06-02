// File: SourceSelector.kt
// Purpose: Allows the user to pick which Islamic database (fatwa source) to query.
// Layer: Layer 1 — Android App (UI Component)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rushdululilm.app.ui.theme.RushdulIlmTheme

/**
 * A horizontal scrolling list of chips to select a knowledge source.
 *
 * @param selectedSource The ID of the currently selected source.
 * @param onSourceSelected Function called when a new source is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class) // FilterChip is still marked experimental
@Composable
fun SourceSelector(
    selectedSource: String,
    onSourceSelected: (String) -> Unit
) {
    // List of sources mapping ID to display name
    val sources = listOf(
        "all" to "All Sources",
        "islamqa_info" to "IslamQA.info (Neutral)",
        "islamqa_org" to "IslamQA.org (Neutral)",
        "deoband" to "Darul Ifta Deoband (Hanafi)"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Title in Telugu and English
        Text(
            text = "మూలం: / Source:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // LazyRow is a horizontally scrollable list that only loads items as they appear on screen.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            // Add padding so the first and last items don't touch the very edge of the screen
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            // Add space between each chip
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(sources) { (sourceId, sourceName) ->
                // FilterChip is a pill-shaped button that can be selected or unselected.
                FilterChip(
                    selected = selectedSource == sourceId,
                    onClick = { onSourceSelected(sourceId) },
                    label = { 
                        Text(
                            text = sourceName,
                            style = MaterialTheme.typography.labelSmall // 16sp minimum
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        // When selected, use IslamicGreen background with white text
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

// Preview to test the component
@Preview(showBackground = true)
@Composable
fun SourceSelectorPreview() {
    RushdulIlmTheme {
        SourceSelector(
            selectedSource = "islamqa_info",
            onSourceSelected = {}
        )
    }
}
