// File: Color.kt
// Purpose: Defines all the specific colors used in the app, like paint swatches before painting a house.
// Layer: Layer 1 — Android App (UI Theme)
// Depends on: None (Standalone UI Configuration)
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.theme

import androidx.compose.ui.graphics.Color
// ^ Jetpack Compose library class that defines colors using red, green, blue, and alpha (opacity) channels

// 🏛️ CONCEPT: Color definitions in Compose use ARGB Hex values, where the first two digits (after FF)
//    represent the alpha (transparency) channel, followed by Red, Green, and Blue hex values.
// 🏛️ ANALOGY: Color.kt is like a paint mixing station at a hardware store. 
//    Instead of asking painters to mix their own green on the wall, we mix "IslamicGreen" once, name it, and hand it to everyone.
val IslamicGreen = Color(0xFF2E7D32)
// ^ Defines a constant color named IslamicGreen (HEX #2E7D32 - dark green) for primary buttons and success banners
// ^ Hex 0xFF2E7D32: 'FF' is full opacity, '2E' is red, '7D' is green, '32' is blue

val QuranicBlue = Color(0xFF1565C0)
// ^ Defines a constant color named QuranicBlue (HEX #1565C0 - calm blue) for clickable links and citations
// ^ Hex 0xFF1565C0: 'FF' is full opacity, '15' is red, '65' is green, 'C0' is blue

val OfflineOrange = Color(0xFFE65100)
// ^ Defines a constant color named OfflineOrange (HEX #E65100 - bright orange) for the offline status banner
// ^ Hex 0xFFE65100: 'FF' is full opacity, 'E6' is red, '51' is green, '00' is blue

val ErrorRed = Color(0xFFB71C1C)
// ^ Defines a constant color named ErrorRed (HEX #B71C1C - error red) for warning states and network connection errors
// ^ Hex 0xFFB71C1C: 'FF' is full opacity, 'B7' is red, '1C' is green, '1C' is blue

val BackgroundCream = Color(0xFFFFF8E1)
// ^ Defines a constant color named BackgroundCream (HEX #FFF8E1 - soft warm yellow/white) for screen backgrounds
// ^ Hex 0xFFFFF8E1: 'FF' is full opacity, 'FF' is red, 'F8' is green, 'E1' is blue

val TextPrimary = Color(0xFF1A1A1A)
// ^ Defines a constant color named TextPrimary (HEX #1A1A1A - soft black/grey) for high contrast readable text
// ^ Hex 0xFF1A1A1A: 'FF' is full opacity, '1A' is red, '1A' is green, '1A' is blue
