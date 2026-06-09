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
import androidx.compose.ui.res.stringResource
import com.rushdululilm.app.R
import com.rushdululilm.app.ui.theme.RushdulIlmTheme

/**
 * 📚 A horizontal scrolling list of 'chips' (pill-shaped buttons) to select a knowledge source.
 * Analogy: Like choosing a channel on a TV — you pick which "channel" of Islamic info you want.
 *
 * @param selectedSource The internal key of the currently selected source ("all", "neutral", "hanafi").
 * @param onSourceSelected Function called when a new source chip is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class) // FilterChip is still marked experimental by Google
@Composable
fun SourceSelector(
    selectedSource: String,
    onSourceSelected: (String) -> Unit
) {
    // 📝 This list maps our internal 'code keys' to the human-readable names shown on screen.
    // 'all' -> Shows all possible fatwa databases.
    // 'neutral' -> Shows sources that aren't strictly one Madhab.
    // 'hanafi' -> Shows sources that follow the Hanafi school (like Deoband).
    val sources = listOf(
        "all" to stringResource(R.string.source_all),
        "neutral" to stringResource(R.string.madhab_neutral),
        "hanafi" to stringResource(R.string.madhab_hanafi)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // 🏷️ Section Title: "Source: / మూలం:" (English + Telugu)
        Text(
            text = stringResource(R.string.source_selector_label),
            style = MaterialTheme.typography.bodyLarge, // 18sp
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // 🏃 LazyRow is a horizontally scrollable list.
        // It's "Lazy" because it only builds the chips that are actually on the screen.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            // 📏 Add padding so the chips don't touch the very edge of the phone screen
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            // ↔️ Add a small gap between each chip
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            // 'items' loop through our list of sources above
            items(sources) { (sourceId, sourceName) ->
                // 💊 FilterChip is the pill-shaped button
                FilterChip(
                    // If this chip's ID matches the selected ID, it turns green
                    selected = selectedSource == sourceId,
                    // When tapped, tell the ViewModel which one was picked
                    onClick = { onSourceSelected(sourceId) },
                    label = { 
                        // The text inside the chip (e.g., "Hanafi (Deoband)")
                        Text(
                            text = sourceName,
                            style = MaterialTheme.typography.labelSmall // 16sp minimum
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        // 🎨 When selected: IslamicGreen background with white text
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
