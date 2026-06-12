package com.rushdululilm.app.ui.screens

// File: NavGraph.kt
// Purpose: The "Traffic Controller" of our app - it connects all our screens together.
// Layer: Layer 1 — Android App (UI)
// Depends on: Routes.kt, HomeScreen.kt, AnswerScreen.kt, VideoLibraryScreen.kt, SettingsScreen.kt, R
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.compose.foundation.layout.padding
// ^ Modifier extension to add padding spacings around layouts
import androidx.compose.material.icons.Icons
// ^ Material Design icons library index registry
import androidx.compose.material.icons.filled.Home
// ^ Standard Home icon graphic asset
import androidx.compose.material.icons.filled.PlayArrow
// ^ Standard Play Arrow icon graphic asset used for the Video Library
import androidx.compose.material.icons.filled.Settings
// ^ Standard Settings cogwheel icon graphic asset
import androidx.compose.material3.Icon
// ^ Composable widget that draws vector graphic icons with color tints
import androidx.compose.material3.NavigationBar
// ^ Material3 standard Bottom Navigation Bar container widget
import androidx.compose.material3.NavigationBarItem
// ^ Composable widget representing a single selectable item tab in the Bottom Navigation Bar
import androidx.compose.material3.Scaffold
// ^ Layout structural component managing top bars, bottom navigation bar, and main page content spacing
import androidx.compose.material3.Text
// ^ Composable widget that draws readable text on the screen
import androidx.compose.runtime.Composable
// ^ Annotation marking functions that define layout drawing blocks in Jetpack Compose
import androidx.compose.runtime.getValue
// ^ Kotlin getter extension property for Compose state variables (allows simple var access instead of .value)
import androidx.compose.ui.Modifier
// ^ Compose builder class to add decorations, clicks, sizes, and padding details to widgets
import androidx.compose.ui.res.stringResource
// ^ Compose utility function to load localized string texts from strings.xml at runtime
import androidx.navigation.NavDestination.Companion.hierarchy
// ^ Navigation library helper extension to retrieve the parent-child destination hierarchy paths
import androidx.navigation.NavGraph.Companion.findStartDestination
// ^ Navigation helper to find the entry point route of the active navigation graph
import androidx.navigation.compose.NavHost
// ^ Navigation container widget that displays the current screen composable mapped to the active route
import androidx.navigation.compose.composable
// ^ Navigation builder extension mapping a route string to a screen composable function
import androidx.navigation.compose.currentBackStackEntryAsState
// ^ Observes the navigation backstack as a State flow to identify which screen is active
import androidx.navigation.compose.rememberNavController
// ^ Compose function creating a NavController instance that survives screen redraws (recompositions)
import androidx.navigation.NavType
// ^ Navigation parameter typing
import androidx.navigation.navArgument
// ^ Navigation argument builder
import androidx.compose.material.icons.filled.List
// ^ Standard list icon
import com.rushdululilm.app.R
// ^ Imports our app's generated Resource registry

// 🏛️ CONCEPT: Navigation in Jetpack Compose uses a NavController to trigger screen changes, 
//    and a NavHost to draw the Composable screen linked to the active route.
// 🏛️ ANALOGY: NavGraph is like a TV setup. The Scaffold is the TV chassis. 
//    The NavigationBar is the remote control buttons, and the NavHost is the picture screen that displays different channels (composables) when you click a button.
@Composable
// ^ Annotation indicating that this function represents a UI Composable drawing layout
fun RushdulIlmNavGraph() {
// ^ Declares RushdulIlmNavGraph function
    val navController = rememberNavController()
    // ^ Instantiates the NavController "remote control" to manage screen changes and history

    Scaffold(
    // ^ Sets up standard screen structural layout containing slot allocations for top and bottom bars
        bottomBar = {
        // ^ Passes bottom bar widget block
            NavigationBar {
            // ^ Instantiates standard Bottom Navigation Bar container
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                // ^ Observes backstack updates to identify which screen destination is active
                
                val currentDestination = navBackStackEntry?.destination
                // ^ Retrieves the active destination screen object (or null if empty)

                NavigationBarItem(
                // ^ Draws Home Screen navigation tab button
                    icon = { 
                    // ^ Passes icon widget block
                        Icon(
                        // ^ Draws Home vector icon
                            imageVector = Icons.Default.Home, 
                            // ^ standard Home icon asset
                            contentDescription = stringResource(R.string.nav_home)
                            // ^ accessibility reader description
                        ) 
                    },
                    // ^ Ends icon parameter
                    label = { Text(stringResource(R.string.nav_home)) },
                    // ^ Draws bilingual tab name under the icon
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                    // ^ Highlights tab if the active destination route matches HOME route
                    onClick = {
                    // ^ Click callback triggered when the user taps the Home tab
                        navController.navigate(Routes.HOME) {
                        // ^ Commands the controller to load the Home screen
                            popUpTo(navController.graph.findStartDestination().id) {
                            // ^ Pops up to the start destination of the graph to avoid building up a large history stack
                                saveState = true
                                // ^ Saves state (inputs, scroll positions) of popped screens
                            }
                            // ^ Ends popUpTo configuration
                            launchSingleTop = true
                            // ^ Avoids opening duplicate copies of the screen if clicked repeatedly
                            restoreState = true
                            // ^ Restores saved state when returning to a previously loaded tab
                        }
                        // ^ Ends navigation configurations
                    }
                    // ^ Ends onClick callback
                )
                // ^ Ends Home NavigationBarItem

                NavigationBarItem(
                // ^ Draws Answers History navigation tab button
                    icon = { 
                    // ^ Passes icon widget block
                        Icon(
                        // ^ Draws List vector icon
                            imageVector = Icons.Default.List, 
                            // ^ standard List icon asset
                            contentDescription = stringResource(R.string.nav_answers)
                            // ^ accessibility reader description
                        ) 
                    },
                    label = { Text(stringResource(R.string.nav_answers)) },
                    // ^ Draws bilingual tab name under the icon
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.ANSWERS_HISTORY } == true,
                    // ^ Highlights tab if active destination matches ANSWERS_HISTORY route
                    onClick = {
                    // ^ Click callback to navigate to Answers History
                        navController.navigate(Routes.ANSWERS_HISTORY) {
                        // ^ Commands controller to load Answers History
                            popUpTo(navController.graph.findStartDestination().id) {
                            // ^ Pops history stack back to start destination
                                saveState = true
                                // ^ Saves active state
                            }
                            launchSingleTop = true
                            // ^ Avoids duplicates
                            restoreState = true
                            // ^ Restores state
                        }
                    }
                )
                // ^ Ends Answers History NavigationBarItem

                NavigationBarItem(
                // ^ Draws Video Library navigation tab button
                    icon = { 
                    // ^ Passes icon widget block
                        Icon(
                        // ^ Draws PlayArrow vector icon
                            imageVector = Icons.Default.PlayArrow, 
                            // ^ standard PlayArrow icon asset
                            contentDescription = stringResource(R.string.nav_videos)
                            // ^ accessibility reader description
                        ) 
                    },
                    label = { Text(stringResource(R.string.nav_videos)) },
                    // ^ Draws bilingual tab name under the icon
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.VIDEO_LIBRARY } == true,
                    // ^ Highlights tab if active destination matches VIDEO_LIBRARY route
                    onClick = {
                    // ^ Click callback to navigate to Video Library
                        navController.navigate(Routes.VIDEO_LIBRARY) {
                        // ^ Commands controller to load Video Library
                            popUpTo(navController.graph.findStartDestination().id) {
                            // ^ Pops history stack back to start destination
                                saveState = true
                                // ^ Saves active state
                            }
                            launchSingleTop = true
                            // ^ Avoids duplicates
                            restoreState = true
                            // ^ Restores state
                        }
                    }
                )
                // ^ Ends Video Library NavigationBarItem

                NavigationBarItem(
                // ^ Draws Settings Screen navigation tab button
                    icon = { 
                    // ^ Passes icon widget block
                        Icon(
                        // ^ Draws Settings vector icon
                            imageVector = Icons.Default.Settings, 
                            // ^ standard Settings cogwheel icon asset
                            contentDescription = stringResource(R.string.nav_settings)
                            // ^ accessibility reader description
                        ) 
                    },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    // ^ Draws bilingual tab name under the icon
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
                    // ^ Highlights tab if active destination matches SETTINGS route
                    onClick = {
                    // ^ Click callback to navigate to Settings
                        navController.navigate(Routes.SETTINGS) {
                        // ^ Commands controller to load Settings screen
                            popUpTo(navController.graph.findStartDestination().id) {
                            // ^ Pops history stack back to start destination
                                saveState = true
                                // ^ Saves active state
                            }
                            launchSingleTop = true
                            // ^ Avoids duplicates
                            restoreState = true
                            // ^ Restores state
                        }
                    }
                )
                // ^ Ends Settings NavigationBarItem
            }
            // ^ Ends NavigationBar layout
        }
        // ^ Ends bottomBar block
    ) { innerPadding ->
    // ^ innerPadding provides screen boundary spacing so layouts do not render behind the bottomBar
        NavHost(
        // ^ Renders the current screen destination matching the active route
            navController = navController,
            // ^ Passes the remote control navController instance
            startDestination = Routes.HOME, 
            // ^ Sets the starting default screen to HOME
            modifier = Modifier.padding(innerPadding) 
            // ^ Applies Scaffold innerPadding offsets around the screen frame
        ) {
        // ^ Starts NavHost body
            composable(Routes.HOME) {
            // ^ Registers the HOME route link
                HomeScreen(navController)
                // ^ Draws the HomeScreen and passes navController reference
            }
            // ^ Ends HOME composable mapping

            composable(
                route = "${Routes.ANSWER}?answerId={answerId}",
                arguments = listOf(navArgument("answerId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
            // ^ Registers the ANSWER route link with optional answerId parameter
                val answerId = backStackEntry.arguments?.getString("answerId")
                AnswerScreen(navController, answerId)
                // ^ Draws the AnswerScreen detail view
            }
            // ^ Ends ANSWER composable mapping
            
            composable(Routes.ANSWERS_HISTORY) {
            // ^ Registers the ANSWERS_HISTORY route link
                AnswersHistoryScreen(navController)
                // ^ Draws the AnswersHistoryScreen list view
            }
            // ^ Ends ANSWERS_HISTORY composable mapping

            composable(Routes.VIDEO_LIBRARY) {
            // ^ Registers the VIDEO_LIBRARY route link
                VideoLibraryScreen(navController)
                // ^ Draws the VideoLibraryScreen list view
            }
            // ^ Ends VIDEO_LIBRARY composable mapping

            composable(Routes.SETTINGS) {
            // ^ Registers the SETTINGS route link
                SettingsScreen(navController)
                // ^ Draws the SettingsScreen configuration panel
            }
            // ^ Ends SETTINGS composable mapping
        }
        // ^ Ends NavHost block
    }
    // ^ Ends Scaffold content block
}
// ^ Ends RushdulIlmNavGraph Composable function