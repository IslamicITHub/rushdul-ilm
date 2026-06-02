package com.rushdululilm.app.ui.components

// File: VideoCard.kt
// Purpose: Displays a single video entry in the video library list.
// Layer: Layer 1 — Android App (UI Component)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rushdululilm.app.model.RelatedVideo
import com.rushdululilm.app.ui.theme.IslamicGreen

/**
 * A horizontal card displaying a video thumbnail, title, scholar, and duration.
 */
@Composable
fun VideoCard(video: RelatedVideo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Placeholder (Grey box with play icon)
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 60.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "▶", color = Color.DarkGray)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                // Scholar Name
                Text(
                    text = video.scholarName,
                    style = MaterialTheme.typography.labelSmall, // 16sp min
                    color = IslamicGreen
                )
                // Video Title
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), // 18sp
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                // Duration
                val minutes = video.durationSeconds / 60
                val seconds = video.durationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.labelSmall, // 16sp min
                    color = Color.Gray
                )
            }
        }
    }
}
