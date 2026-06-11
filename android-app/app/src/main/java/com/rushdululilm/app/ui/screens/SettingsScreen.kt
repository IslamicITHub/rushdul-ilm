// File: SettingsScreen.kt
// Purpose: Displays the app settings and offline download options
// Layer: Layer 1 — Android App (UI)
// Depends on: NavGraph.kt, SettingsViewModel.kt, Routes.kt, AppLanguage.kt
// Created: 2026-05-30 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.screens
// ^ Package declaration defining the namespace where this file lives in the application

import androidx.compose.foundation.layout.*
// ^ Imports standard Compose layouts (Column, Row, Box, Spacer) and dimensional paddings
import androidx.compose.foundation.lazy.LazyColumn
// ^ Highly efficient vertical list component that draws only items currently visible on the screen
import androidx.compose.foundation.lazy.items
// ^ Compose extension helper function to loop through Kotlin lists inside LazyColumn/LazyRow blocks
import androidx.compose.foundation.selection.selectable
// ^ Modifier enabling selection actions on components like RadioButtons, managing click roles for accessibility
import androidx.compose.foundation.selection.selectableGroup
// ^ Modifier grouping selectable rows together so screen readers understand they are mutually exclusive options
import androidx.compose.foundation.shape.RoundedCornerShape
// ^ Layout builder class used to round card corners and make buttons look like smooth pills
import androidx.compose.material.icons.Icons
// ^ Object containing the standard catalog of Material design system graphic vector icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// ^ Auto-mirrored back arrow icon that flips dynamically for right-to-left (RTL) reading layouts
import androidx.compose.material.icons.filled.Download
// ^ Material design icon representing downloads, used for downloading offline databases
import androidx.compose.material3.*
// ^ Imports all core components from Google's Material Design 3 library (TopAppBar, Button, Text, Card)
import androidx.compose.runtime.Composable
// ^ Compiler annotation signaling that this function constructs drawing operations in Compose
import androidx.compose.runtime.collectAsState
// ^ Extension mapping StateFlow dynamic streams into Compose State objects to trigger automatic UI redraws
import androidx.compose.runtime.getValue
// ^ Kotlin property delegate getter enabling direct read of Compose state values without calling .value
import androidx.compose.ui.Alignment
// ^ Layout utility class to align children within container structures (e.g. Center, TopStart)
import androidx.compose.ui.Modifier
// ^ Main builder class used to chain layout sizes, padding margins, shadow depths, and click events
import androidx.compose.ui.graphics.Color
// ^ Graphics color representation class supporting 32-bit Hex integer values
import androidx.compose.ui.res.stringResource
// ^ Composable helper fetching localized string texts from the values XML resources at runtime
import androidx.compose.ui.semantics.Role
// ^ Specifies component interactive behavior type to accessibility systems (e.g. RadioButton, Checkbox)
import androidx.compose.ui.text.font.FontWeight
// ^ Type style class defining standard character weight thickness (e.g. Bold, ExtraBold, Normal)
import androidx.compose.ui.unit.dp
// ^ Converts standard numbers into density-independent pixels for device display scaling
import androidx.compose.ui.unit.sp
// ^ Converts standard numbers into scale-independent pixels for user font-size configurations
import androidx.hilt.navigation.compose.hiltViewModel
// ^ Dagger Hilt integration function that automatically creates or loads viewmodels scoped to navigation
import androidx.navigation.NavController
// ^ Class managing navigation actions and backing histories between app screens
import com.rushdululilm.app.R
// ^ Generated Android resources registry mapping references to raw string and layouts indices
import com.rushdululilm.app.model.AppLanguage
// ^ Enum containing all supported application languages (Telugu, Urdu, English)
import com.rushdululilm.app.viewmodel.SettingsViewModel
// ^ Brain ViewModel class that processes application configurations and updates user preferences repository

// 🏛️ CONCEPT: User preference management binds UI controls to local repository storage.
//    Changes selected in this screen apply globally across the application.
// 🏛️ ANALOGY: SettingsScreen is like a control room inside a factory.
//    It has toggle switches (large text, autoplay) and dial buttons (language, madhab).
//    It also has download meters to bring offline crates (IslamQA/Deoband data) into the local warehouse.
@OptIn(ExperimentalMaterial3Api::class)
// ^ Tells compiler we are opting into Experimental Material 3 APIs (like TopAppBar configuration)
@Composable
// ^ Annotation identifying this function as a Compose layout drawing interface block
fun SettingsScreen(
// ^ Declares SettingsScreen composable entry point
    navController: NavController,
    // ^ Parameter carrying NavController for navigating back to previous screens
    viewModel: SettingsViewModel = hiltViewModel()
    // ^ Supplies the active SettingsViewModel instance injected by Hilt by default
) {
// ^ Starts SettingsScreen body block

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    // ^ Observes selectedLanguage StateFlow from ViewModel, mapping it to a standard variable delegate
    
    val selectedMadhab by viewModel.selectedMadhab.collectAsState()
    // ^ Observes selectedMadhab StateFlow from ViewModel to update active madhab filter choices
    
    val isAutoPlayEnabled by viewModel.isAutoPlayEnabled.collectAsState()
    // ^ Observes isAutoPlayEnabled StateFlow to update active autoplay toggle switch
    
    val isLargeTextEnabled by viewModel.isLargeTextEnabled.collectAsState()
    // ^ Observes isLargeTextEnabled StateFlow to update font-size scaling toggle switch

    Scaffold(
    // ^ Structural layout widget managing standard material screen scaffold elements
        topBar = {
        // ^ Attaches top app bar header section to the Scaffold layout
            TopAppBar(
            // ^ Displays standard Material 3 screen header bar
                title = { Text(stringResource(R.string.settings_label), fontWeight = FontWeight.Bold) },
                // ^ Binds localized screen label resource to the top bar title with bold font style
                navigationIcon = {
                // ^ Attaches action icon to the left side of the top bar
                    IconButton(onClick = { navController.popBackStack() }) {
                    // ^ Renders circular button executing back-navigation pop stack on press
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.icon_desc_back))
                        // ^ Draws standard RTL-compatible back arrow icon with local screen reader label
                    }
                    // ^ Ends navigation button layout
                }
                // ^ Ends navigationIcon definition
            )
            // ^ Ends TopAppBar composable definition
        }
        // ^ Ends topBar configuration
    ) { paddingValues ->
    // ^ Exposes padding spacing calculations to prevent content overlap with headers/footers
        LazyColumn(
        // ^ Vertically scrollable item layout mapping only visible components to conserve RAM
            modifier = Modifier
            // ^ Chain modifiers to style the main scrolling list canvas
                .fillMaxSize()
                // ^ Stretches list size to occupy total available scaffold window space
                .padding(paddingValues)
                // ^ Subtracts header/footer bar height margins from active canvas area
                .padding(16.dp),
                // ^ Adds 16dp margins surrounding list contents inside the scroll bounds
            verticalArrangement = Arrangement.spacedBy(24.dp)
            // ^ Places a neat 24dp gap space between adjacent section items
        ) {
        // ^ Starts LazyColumn scope
            item {
            // ^ Defines a single static item block inside the vertical scroll list for language settings
                SettingsSectionTitle(stringResource(R.string.language_selector_label))
                // ^ Renders language section title label widget
                
                val languages = AppLanguage.entries
                // ^ Fetches the list of all supported application languages from the AppLanguage enum
                
                Column(Modifier.selectableGroup()) {
                // ^ Arranges language radio buttons vertically, grouping them for accessibility
                    languages.forEach { language ->
                    // ^ Loops through every supported language entry
                        SettingsRadioButton(
                        // ^ Instantiates reusable radio button row component
                            label = stringResource(language.displayNameRes),
                            // ^ Loads localized language name text label
                            selected = selectedLanguage == language,
                            // ^ Checks if this language matches the current preference setting
                            onClick = { viewModel.onLanguageSelected(language) }
                            // ^ Triggers preference update in ViewModel when selected
                        )
                        // ^ Ends SettingsRadioButton layout
                    }
                    // ^ Ends loop
                }
                // ^ Ends language options Column
            }
            // ^ Ends language item block

            item {
            // ^ Defines a single static item block inside the vertical scroll list for madhab preference
                SettingsSectionTitle(stringResource(R.string.madhab_preference))
                // ^ Renders madhab preference section title label widget
                
                val madhabDescription by viewModel.madhabDescription.collectAsState()
                // ^ Observes madhab description text resources from ViewModel dynamically
                
                val madhabOptions = listOf(
                // ^ Defines a static list mapping internal keys to localized display labels
                    "all" to stringResource(R.string.source_all),
                    // ^ Pair mapping key "all" to localized "All Sources" label
                    "neutral" to stringResource(R.string.madhab_neutral),
                    // ^ Pair mapping key "neutral" to localized "Neutral" label
                    "hanafi" to stringResource(R.string.madhab_hanafi)
                    // ^ Pair mapping key "hanafi" to localized "Hanafi Madhab" label
                )
                // ^ Ends madhabOptions list definition
                
                Column(Modifier.selectableGroup()) {
                // ^ Arranges madhab radio buttons vertically, grouping them for accessibility
                    madhabOptions.forEach { (key, label) ->
                    // ^ Loops through every madhab key-label pair entry
                        SettingsRadioButton(
                        // ^ Instantiates reusable radio button row component
                            label = label,
                            // ^ Sets display name label text
                            selected = selectedMadhab == key,
                            // ^ Checks if this option key matches the current preference setting
                            onClick = { viewModel.onMadhabSelected(key) }
                            // ^ Triggers preference update in ViewModel when selected
                        )
                        // ^ Ends SettingsRadioButton layout
                    }
                    // ^ Ends loop
                }
                // ^ Ends madhab options Column

                Card(
                // ^ Renders card layout summarizing the selected madhab explanation details
                    modifier = Modifier
                    // ^ Modifier configurations for card layout
                        .fillMaxWidth()
                        // ^ Stretches card fully across screen content width
                        .padding(top = 12.dp),
                        // ^ Adds 12dp top margin space separating card from radio buttons
                    colors = CardDefaults.cardColors(
                    // ^ Sets card container background styles
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        // ^ Applies semi-transparent background tone to show technical context
                    ),
                    // ^ Ends colors parameter
                    shape = RoundedCornerShape(12.dp)
                    // ^ Rounds card corners with 12dp radius
                ) {
                // ^ Starts Card layout body content
                    Row(
                    // ^ Horizontal layout containing icon and explanation text
                        modifier = Modifier.padding(16.dp),
                        // ^ Adds 16dp padding space surrounding row contents
                        verticalAlignment = Alignment.CenterVertically
                        // ^ Aligns icon and description along vertical centerline
                    ) {
                    // ^ Starts Row contents layout
                        Text("💡", fontSize = 24.sp)
                        // ^ Draws lightbulb icon using 24sp size to capture user attention
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        // ^ Adds 12dp blank gap spacing
                        
                        Text(
                        // ^ Renders description text explaining what the choice means
                            text = stringResource(madhabDescription),
                            // ^ Fetches dynamic explanation text from string XML resources
                            fontSize = 16.sp,
                            // ^ Applies readable 16sp font size
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            // ^ Applies surface contrast color matching the theme
                            lineHeight = 22.sp
                            // ^ Configures 22sp line height for comfortable text reading
                        )
                        // ^ Ends explanation Text widget
                    }
                    // ^ Ends Row layout
                }
                // ^ Ends Card container widget
            }
            // ^ Ends madhab item block

            item {
            // ^ Defines a single static item block inside the vertical scroll list for offline downloads
                SettingsSectionTitle(stringResource(R.string.offline_knowledge_title))
                // ^ Renders offline knowledge section title label widget
                
                DownloadItem(
                // ^ Renders download component for IslamQA offline database
                    name = "IslamQA.info",
                    // ^ Sets database name label
                    size = "~500MB",
                    // ^ Sets expected download package file size
                    status = stringResource(R.string.not_downloaded_status),
                    // ^ Sets current download status text label
                    onDownload = { /* Placeholder */ }
                    // ^ Action listener placeholder for future database downloader triggers
                )
                // ^ Ends DownloadItem
                
                Spacer(modifier = Modifier.height(12.dp))
                // ^ Adds 12dp spacing height block
                
                DownloadItem(
                // ^ Renders download component for Darul Ifta Deoband offline database
                    name = "Darul Ifta Deoband",
                    // ^ Sets database name label
                    size = "~200MB",
                    // ^ Sets expected download package file size
                    status = stringResource(R.string.downloaded_status),
                    // ^ Sets current download status text label
                    onDownload = { /* Placeholder */ }
                    // ^ Action listener placeholder for future database downloader triggers
                )
                // ^ Ends DownloadItem
            }
            // ^ Ends offline database item block

            item {
            // ^ Defines a single static item block inside the vertical scroll list for general app settings
                SettingsSectionTitle(stringResource(R.string.app_settings_title))
                // ^ Renders app settings section title label widget
                
                SettingsSwitch(
                // ^ Renders switch widget row controlling video autoplay settings
                    label = stringResource(R.string.setting_autoplay),
                    // ^ Loads localized Autoplay setting title text
                    checked = isAutoPlayEnabled,
                    // ^ Sets checked switch state from preferences flow
                    onCheckedChange = { viewModel.onAutoPlayToggled(it) }
                    // ^ Updates autoplay preference in ViewModel when toggled
                )
                // ^ Ends SettingsSwitch layout
                
                SettingsSwitch(
                // ^ Renders switch widget row controlling large text accessibility settings
                    label = stringResource(R.string.setting_large_text),
                    // ^ Loads localized Large Text setting title text
                    checked = isLargeTextEnabled,
                    // ^ Sets checked switch state from preferences flow
                    onCheckedChange = { viewModel.onLargeTextToggled(it) }
                    // ^ Updates large text preference in ViewModel when toggled
                )
                // ^ Ends SettingsSwitch layout
                
                Spacer(modifier = Modifier.height(16.dp))
                // ^ Adds 16dp spacing height block
                
                Text(
                // ^ Renders static application version label at the bottom
                    text = stringResource(R.string.app_version),
                    // ^ Loads localized version string prefix (e.g. Version 1.0.0)
                    fontSize = 16.sp,
                    // ^ Applies 16sp font size
                    color = Color.Gray
                    // ^ Colors version text gray to signal secondary context
                )
                // ^ Ends version Text widget
            }
            // ^ Ends app settings item block
        }
        // ^ Ends vertical LazyColumn list container
    }
    // ^ Ends Scaffold contents block
}
// ^ Ends SettingsScreen function block

// 🏛️ CONCEPT: Reusable UI headers isolate title font properties from parent screen lists.
//    Isolating styling into clean sub-components makes screens look highly uniform.
// 🏛️ ANALOGY: SettingsSectionTitle is like a large bold sign hanging above a supermarket aisle.
//    It tells the user exactly what category of items (Language, Madhab, Downloads) they can find below it.
@Composable
// ^ Annotation signaling this function draws UI widgets in Compose
fun SettingsSectionTitle(title: String) {
// ^ Declares custom SettingsSectionTitle composable accepting section name string
    Text(
    // ^ Draws Section Title Text
        text = title,
        // ^ Feeds raw text label passed from parent page
        fontSize = 20.sp,
        // ^ Applies bold 20sp heading typography
        fontWeight = FontWeight.ExtraBold,
        // ^ Sets extra bold weight thickness
        color = MaterialTheme.colorScheme.primary,
        // ^ Sets title color to main theme primary brand color
        modifier = Modifier.padding(bottom = 8.dp)
        // ^ Appends 8dp spacing beneath title to separate it from options
    )
    // ^ Ends Section Title Text
}
// ^ Ends SettingsSectionTitle function block

// 🏛️ CONCEPT: Custom radio button rows increase accessibility touch targets.
//    Selecting any portion of the horizontal row registers the click event.
// 🏛️ ANALOGY: SettingsRadioButton is like a row of key options where clicking the name selects the key.
//    You don't need to press the tiny circle icon directly; pressing the entire row executes the toggle action.
@Composable
// ^ Annotation signaling this function draws UI widgets in Compose
fun SettingsRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
// ^ Declares custom SettingsRadioButton composable accepting labels, status, and click behaviors
    Row(
    // ^ Horizontally aligns radio button circle and text description
        modifier = Modifier
        // ^ Formats row layouts and accessibility role parameters
            .fillMaxWidth()
            // ^ Forces the row to stretch fully across screen width
            .height(56.dp)
            // ^ Sets fixed 56dp height to ensure easy finger tap target size
            .selectable(
            // ^ Enables select actions on row level
                selected = selected,
                // ^ Sets selected status
                onClick = onClick,
                // ^ Sets action listener callback on selection
                role = Role.RadioButton
                // ^ Declares this layout represents a Radio Button element to accessibility tools
            )
            // ^ Ends selectable parameters mapping
            .padding(horizontal = 8.dp),
            // ^ Adds 8dp horizontal padding space inside the borders
        verticalAlignment = Alignment.CenterVertically
        // ^ Center-aligns radio circle and label text vertically
    ) {
    // ^ Starts Row contents layout
        RadioButton(
        // ^ Standard Material 3 RadioButton circle indicator
            selected = selected,
            // ^ Sets selection active/inactive visual status
            onClick = null
            // ^ Disables direct click handler on icon circle to redirect clicks to full row area
        )
        // ^ Ends RadioButton icon widget
        
        Spacer(modifier = Modifier.width(16.dp))
        // ^ Places 16dp spacing gap between circle icon and label text
        
        Text(text = label, fontSize = 18.sp)
        // ^ Renders option name label using large, readable 18sp font sizes
    }
    // ^ Ends Row layout
}
// ^ Ends SettingsRadioButton function block

// 🏛️ CONCEPT: Custom toggle switch rows align toggles along screen edges automatically.
//    It structures parameters to isolate labels from status controls.
// 🏛️ ANALOGY: SettingsSwitch is like a standard wall light switch.
//    It labels what light it turns on (Autoplay, Large Text) and mounts the toggle slider next to it.
@Composable
// ^ Annotation signaling this function draws UI widgets in Compose
fun SettingsSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
// ^ Declares custom SettingsSwitch composable accepting labels, checked states, and action triggers
    Row(
    // ^ Horizontally aligns label text and toggle slider control
        modifier = Modifier
        // ^ Formats container height and inner spacing margins
            .fillMaxWidth()
            // ^ Forces the row to stretch fully across horizontal screen space
            .height(56.dp)
            // ^ Enforces 56dp height target for accessibility guidelines
            .padding(horizontal = 8.dp),
            // ^ Adds 8dp horizontal padding margins inside row borders
        verticalAlignment = Alignment.CenterVertically,
        // ^ Aligns text and switch toggle along vertical centerline
        horizontalArrangement = Arrangement.SpaceBetween
        // ^ Pushes label text to the left and switch control to the far right edge
    ) {
    // ^ Starts Row contents layout
        Text(text = label, fontSize = 18.sp, modifier = Modifier.weight(1f))
        // ^ Renders setting title name using 18sp size, allocating remaining width to label
        
        Switch(
        // ^ Standard Material 3 Switch slider control
            checked = checked,
            // ^ Sets slider switch active/inactive status
            onCheckedChange = onCheckedChange
            // ^ Runs execution callback in ViewModel when switch is toggled
        )
        // ^ Ends Switch control widget
    }
    // ^ Ends Row layout
}
// ^ Ends SettingsSwitch function block

// 🏛️ CONCEPT: Download widgets organize technical file size data inside structured cards.
//    Organizing details inside cards helps users distinguish database download packages clearly.
// 🏛️ ANALOGY: DownloadItem is like a luggage check tag at a station.
//    It shows the bag name (IslamQA/Deoband), its weight (size), and a stamp button (Download) to grab the bag.
@Composable
// ^ Annotation signaling this function draws UI widgets in Compose
fun DownloadItem(name: String, size: String, status: String, onDownload: () -> Unit) {
// ^ Declares custom DownloadItem composable accepting name, file size, status, and download click callbacks
    val downloadedStatus = stringResource(R.string.downloaded_status)
    // ^ Pulls localized "Downloaded" status string comparison key from XML resources
    
    Card(
    // ^ Material 3 card grouping file details
        modifier = Modifier.fillMaxWidth(),
        // ^ Stretches card size to occupy full screen width boundaries
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        // ^ Adds 2dp elevation shadow for subtle container depth
    ) {
    // ^ Starts Card layout body content
        Row(
        // ^ Horizontally structures text metrics columns and actions buttons
            modifier = Modifier
            // ^ Formats inner spacing margins
                .padding(16.dp)
                // ^ Adds 16dp margins surrounding card elements
                .fillMaxWidth(),
                // ^ Stretches row layout to fill container card width boundaries
            verticalAlignment = Alignment.CenterVertically,
            // ^ Aligns details column and action button along vertical centerline
            horizontalArrangement = Arrangement.SpaceBetween
            // ^ Pushes file description text column to the left and download button to the right
        ) {
        // ^ Starts Row contents layout
            Column(modifier = Modifier.weight(1f)) {
            // ^ Vertically structures database titles and details, allocating maximum row width to it
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                // ^ Renders database name string using bold 18sp typography
                
                Text(text = "$size | $status", fontSize = 16.sp, color = Color.Gray)
                // ^ Renders size metrics and current download status using 16sp gray font
            }
            // ^ Ends Column layout
            
            Button(
            // ^ Interactive action button initiating download or update operations
                onClick = onDownload,
                // ^ Executes downloader callback function passed by parent page when pressed
                colors = ButtonDefaults.buttonColors(
                // ^ Configures download action backgrounds based on file status
                    containerColor = if (status == downloadedStatus) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    // ^ Colors button green if already downloaded, defaults to primary theme color otherwise
                )
                // ^ Ends colors definition mapping
            ) {
            // ^ Starts Button contents layout
                Icon(Icons.Default.Download, contentDescription = null)
                // ^ Draws download arrow icon with null screen reader description (redundant due to text label)
                
                Spacer(Modifier.width(4.dp))
                // ^ Adds 4dp horizontal spacer layout between icon and button label text
                
                Text(if (status == downloadedStatus) stringResource(R.string.update_button_label) else stringResource(R.string.download_button_label))
                // ^ Swaps button label text between "Update" and "Download" dynamically
            }
            // ^ Ends Button
        }
        // ^ Ends Row layout
    }
    // ^ Ends Card container
}
// ^ Ends DownloadItem function block
