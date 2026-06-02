// File: MicButton.kt
// Purpose: A reusable, animated microphone button for capturing user questions.
// Layer: Layer 1 — Android App (UI Component)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.rushdululilm.app.R
import com.rushdululilm.app.ui.theme.RushdulIlmTheme

/**
 * A large, interactive microphone button component.
 *
 * @param isRecording A boolean indicating if the app is currently capturing audio. 
 *                    If true, the button turns red and pulsates.
 * @param onClick A function to execute when the user taps the button.
 */
@Composable
fun MicButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    // Determine the color based on the recording state. 
    // We use the colors defined in our MaterialTheme.
    val buttonColor = if (isRecording) {
        MaterialTheme.colorScheme.error // ErrorRed (#B71C1C)
    } else {
        MaterialTheme.colorScheme.primary // IslamicGreen (#2E7D32)
    }

    // Set up a pulsing animation when recording
    // 'rememberInfiniteTransition' keeps an animation running repeatedly
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        // If recording, pulse up to 1.1x size. Otherwise, stay at 1f.
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            // tween defines the duration and easing of one pulse cycle
            animation = tween(durationMillis = 800),
            // RepeatMode.Reverse makes it shrink back down smoothly
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    // Calculate a height that is roughly 40% of the screen height.
    // LocalConfiguration provides information about the device's screen.
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val buttonHeight = screenHeight * 0.4f

    // We use a Column to stack the button and its label text vertically
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The Box is the actual circular button
        Box(
            modifier = Modifier
                .size(buttonHeight) // Sets width and height to be equal (a square)
                .scale(scale) // Applies the pulsing animation
                .clip(CircleShape) // Cuts the square into a circle
                .background(buttonColor) // Fills the circle with the determined color
                .clickable { onClick() }, // Makes the circle tap-able
            contentAlignment = Alignment.Center // Centers the icon inside the circle
        ) {
            // The microphone icon inside the button
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.icon_desc_mic),
                tint = Color.White,
                modifier = Modifier.size(72.dp) // Large icon size
            )
        }

        // Add some space between the button and the text below it
        Spacer(modifier = Modifier.height(16.dp))

        // Bilingual label for illiterate users (Telugu on top, English below)
        Text(
            text = stringResource(R.string.mic_button_label),
            // Use the large body typography (18sp minimum)
            style = MaterialTheme.typography.bodyLarge,
            // Use the primary text color
            color = MaterialTheme.colorScheme.onBackground,
            // Center the text lines
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// A Preview allows the developer to see the button in Android Studio without running the app.
@Preview(showBackground = true, name = "Not Recording")
@Composable
fun MicButtonPreviewNotRecording() {
    RushdulIlmTheme {
        MicButton(isRecording = false, onClick = {})
    }
}

@Preview(showBackground = true, name = "Recording")
@Composable
fun MicButtonPreviewRecording() {
    RushdulIlmTheme {
        MicButton(isRecording = true, onClick = {})
    }
}
