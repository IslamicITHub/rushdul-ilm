// File: NavGraph.kt
// Purpose: The "Traffic Controller" of our app - it connects all our screens together
// Layer: Layer 1 — Android UI
// Depends on: Routes.kt, HomeScreen.kt, AnswerScreen.kt, VideoLibraryScreen.kt, SettingsScreen.kt
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rushdululilm.app.R

/**
 * RushdulIlmNavGraph is the master UI container that handles switching between screens.
 * 
 * Analogy: Think of this as a TV set. 
 * - The 'NavHost' is the screen showing the current channel.
 * - The 'NavigationBar' at the bottom is the remote control buttons.
 * - Each 'composable' entry is a different TV channel (Screen).
 */
@Composable
fun RushdulIlmNavGraph() {
    // 🎮 The NavController is our "Remote Control" - we use it to change screens
    val navController = rememberNavController()

    // 📱 Scaffold is a basic screen layout structure from Google's Material Design.
    // It provides "slots" for things like a Top Bar, a Bottom Bar, and the Main Content.
    Scaffold(
        bottomBar = {
            // 🏠 The Bottom Navigation Bar allows the user to switch between the 3 main areas
            NavigationBar {
                // We need to know which screen is currently visible to highlight the correct icon
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 🔘 Home Button (Mic Screen)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    // Highlight this button if the current screen is HOME
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                    onClick = {
                        // When clicked, tell the remote control to go to the Home route
                        navController.navigate(Routes.HOME) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )

                // 🔘 Video Library Button
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.nav_videos)) },
                    label = { Text(stringResource(R.string.nav_videos)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.VIDEO_LIBRARY } == true,
                    onClick = {
                        navController.navigate(Routes.VIDEO_LIBRARY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                // 🔘 Settings Button
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
                    onClick = {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // 🖼️ The NavHost is the "Frame" that swaps the actual screen content
        // 'innerPadding' is provided by the Scaffold so our content doesn't hide behind the bottom bar
        NavHost(
            navController = navController,
            startDestination = Routes.HOME, // The app starts on the Home screen
            modifier = Modifier.padding(innerPadding) // Apply the padding from the Scaffold
        ) {
            // 📍 Register the Home Screen route
            composable(Routes.HOME) {
                HomeScreen(navController)
            }

            // 📍 Register the Answer Screen route
            // Note: This screen is not in the bottom bar, it is opened from Home
            composable(Routes.ANSWER) {
                AnswerScreen(navController)
            }

            // 📍 Register the Video Library route
            composable(Routes.VIDEO_LIBRARY) {
                VideoLibraryScreen(navController)
            }

            // 📍 Register the Settings Screen route
            composable(Routes.SETTINGS) {
                SettingsScreen(navController)
            }
        }
    }
}