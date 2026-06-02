// File: android-app/app/src/main/java/com/rushdululilm/app/ui/screens/SettingsScreen.kt
// Purpose: Displays the app settings and offline download options
// Layer: 1 — Android UI Skeleton
// Depends on: NavGraph.kt, SettingsViewModel.kt, Routes.kt
// Created: 2026-05-30 | Modified: 2026-05-31
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rushdululilm.app.viewmodel.SettingsViewModel

/**
 * SettingsScreen allows users to customize their experience and download offline data.
 * Analogy: Like a "Settings" menu on a phone where you change language or brightness.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel() // Hilt injects our ViewModel automatically
) {
    // Observe state from the ViewModel
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedMadhab by viewModel.selectedMadhab.collectAsState()
    val isAutoPlayEnabled by viewModel.isAutoPlayEnabled.collectAsState()
    val isLargeTextEnabled by viewModel.isLargeTextEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings / సెట్టింగ్‌లు", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // LazyColumn is a vertical list that only draws what's visible on screen (efficient!)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Gap between sections
        ) {
            // --- SECTION 1: LANGUAGE SETTINGS ---
            item {
                SettingsSectionTitle("Language / భాష")
                val languages = listOf("Telugu" to "తెలుగు", "Urdu" to "اردو", "Hindi" to "हिंदी", "English" to "English")
                
                Column(Modifier.selectableGroup()) {
                    languages.forEach { (eng, local) ->
                        SettingsRadioButton(
                            label = "$local ($eng)",
                            selected = selectedLanguage == eng,
                            onClick = { viewModel.onLanguageSelected(eng) }
                        )
                    }
                }
            }

            // --- SECTION 2: MADHAB PREFERENCE ---
            item {
                SettingsSectionTitle("Islamic Source Preference / ఇస్లామిక్ మూలం")
                val madhabs = listOf("All Sources", "Neutral Only", "Hanafi (Deoband)")
                
                Column(Modifier.selectableGroup()) {
                    madhabs.forEach { madhab ->
                        SettingsRadioButton(
                            label = madhab,
                            selected = selectedMadhab == madhab,
                            onClick = { viewModel.onMadhabSelected(madhab) }
                        )
                    }
                }
            }

            // --- SECTION 3: OFFLINE KNOWLEDGE DATABASE ---
            item {
                SettingsSectionTitle("Download Offline Knowledge / ఆఫ్‌లైన్ జ్ఞానాన్ని డౌన్‌లోడ్ చేయండి")
                
                DownloadItem(
                    name = "IslamQA.info",
                    size = "~500MB",
                    status = "Not downloaded",
                    onDownload = { /* Placeholder */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                DownloadItem(
                    name = "Darul Ifta Deoband",
                    size = "~200MB",
                    status = "Downloaded",
                    onDownload = { /* Placeholder */ }
                )
            }

            // --- SECTION 4: APP SETTINGS ---
            item {
                SettingsSectionTitle("App Settings / యాప్ సెట్టింగ్‌లు")
                
                SettingsSwitch(
                    label = "Auto-play read aloud / ఆటో-ప్లే రీడ్ అలౌడ్",
                    checked = isAutoPlayEnabled,
                    onCheckedChange = { viewModel.onAutoPlayToggled(it) }
                )
                
                SettingsSwitch(
                    label = "Large text mode / పెద్ద వచన మోడ్",
                    checked = isLargeTextEnabled,
                    onCheckedChange = { viewModel.onLargeTextToggled(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "App Version: 1.0.0-alpha",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Reusable section title for Settings
 */
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/**
 * Reusable RadioButton row with a large touch target (48dp+)
 */
@Composable
fun SettingsRadioButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // High touch target for accessibility
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handle click on the row for better UX
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 18.sp)
    }
}

/**
 * Reusable Switch row for binary settings
 */
@Composable
fun SettingsSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Item for managing offline data downloads
 */
@Composable
fun DownloadItem(name: String, size: String, status: String, onDownload: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "$size | $status", fontSize = 14.sp, color = Color.Gray)
            }
            Button(
                onClick = onDownload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status == "Downloaded") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (status == "Downloaded") "Update" else "Download")
            }
        }
    }
}
