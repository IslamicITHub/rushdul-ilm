// File: MicButton.kt
// Purpose: A reusable, animated microphone button for capturing user questions.
// Layer: Layer 1 — Android App (UI Component)
// Depends on: Color.kt, Theme.kt, R.string labels
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.components

import androidx.compose.animation.core.RepeatMode
// ^ Imports animation repeat settings (e.g., RepeatMode.Reverse to shrink/grow smoothly)
import androidx.compose.animation.core.animateFloat
// ^ Imports extension to animate float values (like scale or alpha transparency)
import androidx.compose.animation.core.infiniteRepeatable
// ^ Imports transition builder to make animations repeat forever
import androidx.compose.animation.core.rememberInfiniteTransition
// ^ Compose function that creates an animation transition state that survives recompositions and runs infinitely
import androidx.compose.animation.core.tween
// ^ Imports time-based animation curve specification (tween)
import androidx.compose.foundation.background
// ^ Modifier extension to draw background color fill on layout shapes
import androidx.compose.foundation.clickable
// ^ Modifier extension to register tap click callbacks on UI components
import androidx.compose.foundation.layout.Arrangement
// ^ Layout configuration for alignment distributions (e.g. SpaceBetween, Center)
import androidx.compose.foundation.layout.Box
// ^ Compose layout container that stacks child widgets directly on top of each other
import androidx.compose.foundation.layout.Column
// ^ Compose layout container that stacks child widgets vertically from top to bottom
import androidx.compose.foundation.layout.Spacer
// ^ Composable layout spacer to add blank spacing gaps between UI elements
import androidx.compose.foundation.layout.fillMaxWidth
// ^ Modifier configuration to expand widget width to fill parent bounds
import androidx.compose.foundation.layout.height
// ^ Modifier configuration to set explicit height dimensions
import androidx.compose.foundation.layout.size
// ^ Modifier configuration to set equal height and width dimensions (for squares/circles)
import androidx.compose.foundation.shape.CircleShape
// ^ Shape representation used to clip layouts into perfect circles
import androidx.compose.material.icons.Icons
// ^ Material Design icons index container
import androidx.compose.material.icons.filled.Mic
// ^ Material Design vector asset for the standard microphone symbol
import androidx.compose.material3.Icon
// ^ Composable widget that draws vector images and handles color tint overlays
import androidx.compose.material3.MaterialTheme
// ^ Component providing access to our custom colors, fonts, and shapes theme configurations
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.runtime.getValue
// ^ Kotlin getter extension property for Compose state variables (allows simple var access instead of .value)
import androidx.compose.ui.Alignment
// ^ Layout alignment definitions (e.g., Center, Top, Bottom)
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.draw.clip
// ^ Modifier configuration to clip widget boundary shapes (e.g., to a circle)
import androidx.compose.ui.draw.scale
// ^ Modifier configuration to scale (shrink/grow) UI elements dynamically
import androidx.compose.ui.graphics.Color
// ^ Compose class defining colors using ARGB Hex values
import androidx.compose.ui.platform.LocalConfiguration
// ^ Compose context wrapper providing configuration properties of the current Android device screen
import androidx.compose.ui.tooling.preview.Preview
// ^ Annotation marking a parameterless Composable function to be rendered inside Android Studio's design preview
import androidx.compose.ui.unit.dp
// ^ Extension property converting numbers to density-independent pixels (dp) for screen density scaling
import androidx.compose.ui.res.stringResource
// ^ Compose utility function to load localized string texts from strings.xml at runtime
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry
import com.rushdululilm.app.ui.theme.RushdulIlmTheme
// ^ Imports our custom theme wrapper function

// 🏛️ CONCEPT: Reusable UI Components are modular Composable functions. 
//    By abstracting the MicButton layout, we can use it on different screens without rewriting the layout or pulse animation code.
// 🏛️ ANALOGY: MicButton is like a calling button on a nurse's desk. 
//    When idle (isRecording is false), it is green and still. When clicked (isRecording is true), it turns red and pulsates (scale animation) to show active status.
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun MicButton(
// ^ Declares MicButton function
    isRecording: Boolean,
    // ^ parameter indicating whether the app is currently capturing speech audio
    onClick: () -> Unit
    // ^ parameter carrying the callback function to execute when the user taps the button
) {
// ^ Starts MicButton body
    val buttonColor = if (isRecording) {
    // ^ Conditional selection of button background color
        MaterialTheme.colorScheme.error 
        // ^ Returns ErrorRed color if recording is active
    } else {
    // ^ Executes if not recording
        MaterialTheme.colorScheme.primary 
        // ^ Returns IslamicGreen color when idle
    }
    // ^ Ends buttonColor check

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    // ^ Instantiates an infinite repeat transition controller state to animate the pulse effect
    
    val scale by infiniteTransition.animateFloat(
    // ^ animates a floating point number repeatedly between initial and target values
        initialValue = 1f,
        // ^ Sets starting scale size to 1.0 (normal size)
        targetValue = if (isRecording) 1.1f else 1f,
        // ^ Sets destination scale size to 1.1 (10% larger) if recording, otherwise keeps it at 1.0
        animationSpec = infiniteRepeatable(
        // ^ Configures the animation to repeat infinitely
            animation = tween(durationMillis = 800),
            // ^ Applies time-based linear ease curves lasting 800 milliseconds per pulse phase
            repeatMode = RepeatMode.Reverse
            // ^ Configures transition to reverse directions (growing then shrinking back down smoothly)
        ),
        // ^ Ends animation spec parameter
        label = "micScale"
        // ^ Sets debug tag label for Android Studio animation inspector
    )
    // ^ Ends scale float animation configuration

    val configuration = LocalConfiguration.current
    // ^ Retrieves device screen details (width, height, orientation)
    
    val screenHeight = configuration.screenHeightDp.dp
    // ^ Extracts the total screen height in density-independent pixels (dp)
    
    val buttonHeight = screenHeight * 0.4f
    // ^ Computes a height exactly equal to 40% of screen height (ensuring large touch targets for low-literacy users)

    Column(
    // ^ Column aligns layout items vertically from top to bottom
        modifier = Modifier.fillMaxWidth(),
        // ^ Configures the column to match parent width
        horizontalAlignment = Alignment.CenterHorizontally,
        // ^ Centers all children widgets horizontally in the column
        verticalArrangement = Arrangement.Center
        // ^ Centers all children widgets vertically in the column
    ) {
    // ^ Starts Column body
        Box(
        // ^ Box container acts as the circular button housing the icon
            modifier = Modifier
            // ^ Instantiates modifier builder
                .size(buttonHeight) 
                // ^ Sets box width and height equal to 40% screen height (making it a square)
                .scale(scale) 
                // ^ Applies the animated scale multiplier factor (causes pulsing effect)
                .clip(CircleShape) 
                // ^ Clips the square Box shape into a perfect circle
                .background(buttonColor) 
                // ^ Fills the circular shape background with our active color (red or green)
                .clickable { onClick() }, 
                // ^ Registers the tap click event listener callback
            contentAlignment = Alignment.Center 
            // ^ Centers the child microphone icon inside the circle bounds
        ) {
        // ^ Starts Box body
            Icon(
            // ^ Draws vector microphone image widget
                imageVector = Icons.Default.Mic,
                // ^ Passes the standard Material Mic vector graphic asset
                contentDescription = stringResource(R.string.icon_desc_mic),
                // ^ Fetches screen-reader description text from strings.xml
                tint = Color.White,
                // ^ Tints the microphone vector graphic color to solid White
                modifier = Modifier.size(72.dp) 
                // ^ Sets the icon vector display size to 72dp for high contrast visibility
            )
            // ^ Ends Icon widget
        }
        // ^ Ends Box widget

        Spacer(modifier = Modifier.height(16.dp))
        // ^ Adds a fixed vertical spacing gap of 16dp below the circular mic button

        Text(
        // ^ Draws the Telugu and English label guidance text
            text = stringResource(R.string.mic_button_label),
            // ^ Fetches localized string from strings.xml
            style = MaterialTheme.typography.bodyLarge,
            // ^ Applies bodyLarge typography style (18sp accessibility size)
            color = MaterialTheme.colorScheme.onBackground,
            // ^ Sets color matching standard text color of background theme
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
            // ^ Centers the text alignment bounds
        )
        // ^ Ends Text widget
    }
    // ^ Ends Column body
}
// ^ Ends MicButton Composable function

@Preview(showBackground = true, name = "Not Recording")
// ^ Preview configuration displaying the idle button with standard white background in Android Studio preview
@Composable
// ^ Annotation indicating Composable drawing function
fun MicButtonPreviewNotRecording() {
// ^ Declares MicButtonPreviewNotRecording function
    RushdulIlmTheme {
    // ^ Wraps preview in our custom theme colors and fonts
        MicButton(isRecording = false, onClick = {})
        // ^ Instantiates MicButton in non-recording state
    }
    // ^ Ends theme wrap
}
// ^ Ends MicButtonPreviewNotRecording function

@Preview(showBackground = true, name = "Recording")
// ^ Preview configuration displaying the active recording button with standard white background in Android Studio preview
@Composable
// ^ Annotation indicating Composable drawing function
fun MicButtonPreviewRecording() {
// ^ Declares MicButtonPreviewRecording function
    RushdulIlmTheme {
    // ^ Wraps preview in our custom theme colors and fonts
        MicButton(isRecording = true, onClick = {})
        // ^ Instantiates MicButton in active recording state
    }
    // ^ Ends theme wrap
}
// ^ Ends MicButtonPreviewRecording function
