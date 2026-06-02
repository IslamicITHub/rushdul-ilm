package com.rushdululilm.app.viewmodel

// File: VideoLibraryViewModel.kt
// Purpose: Manages the state and search filtering for the Video Library screen.
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: AnswerModels.kt
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

import androidx.lifecycle.ViewModel
import com.rushdululilm.app.model.RelatedVideo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * The ViewModel for the Video Library Screen.
 * It holds the list of videos and handles search filtering.
 */
@HiltViewModel
class VideoLibraryViewModel @Inject constructor() : ViewModel() {

    // --- Fake Data ---
    // A hardcoded list of 5 fake videos for UI testing.
    // In Phase 6, this will be replaced by a Room database query.
    private val fakeVideos = listOf(
        RelatedVideo(
            id = "v1",
            title = "Importance of Salah",
            scholarName = "Mufti Menk",
            durationSeconds = 1500, // 25 mins
            filePath = "/sdcard/videos/v1.mp4",
            serverUrl = "http://fake.server/v1.mp4"
        ),
        RelatedVideo(
            id = "v2",
            title = "Understanding Surah Al-Baqarah",
            scholarName = "Nouman Ali Khan",
            durationSeconds = 3600, // 60 mins
            filePath = "/sdcard/videos/v2.mp4",
            serverUrl = "http://fake.server/v2.mp4"
        ),
        RelatedVideo(
            id = "v3",
            title = "Science and Quran",
            scholarName = "Dr. Zakir Naik",
            durationSeconds = 2400, // 40 mins
            filePath = "/sdcard/videos/v3.mp4",
            serverUrl = "http://fake.server/v3.mp4"
        ),
        RelatedVideo(
            id = "v4",
            title = "The Names of Allah",
            scholarName = "Sheikh Omar Suleiman",
            durationSeconds = 1800, // 30 mins
            filePath = "/sdcard/videos/v4.mp4",
            serverUrl = "http://fake.server/v4.mp4"
        ),
        RelatedVideo(
            id = "v5",
            title = "Tafsir of Juz Amma",
            scholarName = "Assim Al-Hakeem",
            durationSeconds = 2100, // 35 mins
            filePath = "/sdcard/videos/v5.mp4",
            serverUrl = "http://fake.server/v5.mp4"
        )
    )

    // --- StateFlow Properties ---

    // The complete list of videos (never changes in this fake setup).
    private val _allVideos = MutableStateFlow<List<RelatedVideo>>(fakeVideos)
    val allVideos: StateFlow<List<RelatedVideo>> = _allVideos.asStateFlow()

    // The current search text typed by the user.
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // The list of videos currently shown on screen (filtered by search query).
    // Initially, it shows all videos.
    private val _filteredVideos = MutableStateFlow<List<RelatedVideo>>(fakeVideos)
    val filteredVideos: StateFlow<List<RelatedVideo>> = _filteredVideos.asStateFlow()

    // --- User Actions ---

    /**
     * Called whenever the user types something into the search bar.
     * It updates the search text and filters the list of videos.
     * 
     * @param query The text currently in the search bar.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        
        // Filter logic: if the query is empty, show all videos.
        // Otherwise, check if the query is in the title or the scholar's name (ignoring uppercase/lowercase).
        if (query.isBlank()) {
            _filteredVideos.value = _allVideos.value
        } else {
            _filteredVideos.value = _allVideos.value.filter { video ->
                video.title.contains(query, ignoreCase = true) || 
                video.scholarName.contains(query, ignoreCase = true)
            }
        }
    }
}
