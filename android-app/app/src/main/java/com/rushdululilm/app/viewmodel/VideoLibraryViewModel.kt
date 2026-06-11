// File: VideoLibraryViewModel.kt
// Purpose: Manages the state and search filtering for the Video Library screen.
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: AnswerModels.kt, StateFlow, ViewModel
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

package com.rushdululilm.app.viewmodel

import androidx.lifecycle.ViewModel
// ^ Base Android Architecture Component class designed to store and manage UI-related data in a lifecycle-aware way
import com.rushdululilm.app.model.RelatedVideo
// ^ Imports the video metadata model containing title, scholar, duration, and path
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Annotation that registers this ViewModel class with Hilt for automated construction
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Flow class that holds a single state value and supports write access
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Flow class that holds a single state value and supports read-only access
import kotlinx.coroutines.flow.asStateFlow
// ^ Extension function to convert a mutable flow into a read-only StateFlow for safe UI observation
import javax.inject.Inject
// ^ Tells Hilt how to inject constructor dependencies

// 🏛️ CONCEPT: VideoLibraryViewModel maintains state for a list of video lectures.
//    It processes search query strings entered in the UI search bar and filters the lists of videos reactively.
// 🏛️ ANALOGY: VideoLibraryViewModel is like a catalog index at a library desk. 
//    When a user types a word in the catalog (searchQuery), the search engine filters the catalog card box (fakeVideos) and displays only matching cards (filteredVideos).
@HiltViewModel
// ^ Tells the Hilt compiler to generate constructor injection configurations for this class
class VideoLibraryViewModel @Inject constructor() : ViewModel() {
// ^ class declaration inheriting from standard ViewModel. constructor is annotated with @Inject for Hilt injection.

    private val fakeVideos = listOf(
    // ^ private read-only list containing 5 mock RelatedVideo records for UI layout testing
        RelatedVideo(
        // ^ Instantiates the first RelatedVideo model
            id = "v1",
            // ^ Video ID
            title = "Importance of Salah",
            // ^ Video title
            scholarName = "Mufti Menk",
            // ^ Scholar speaker name
            durationSeconds = 1500, 
            // ^ Video duration (25 minutes)
            filePath = "/sdcard/videos/v1.mp4",
            // ^ Simulated local storage path
            serverUrl = "http://fake.server/v1.mp4"
            // ^ Simulated remote streaming address
        ),
        // ^ Ends first video declaration
        RelatedVideo(
        // ^ Instantiates the second RelatedVideo model
            id = "v2",
            // ^ Video ID
            title = "Understanding Surah Al-Baqarah",
            // ^ Video title
            scholarName = "Nouman Ali Khan",
            // ^ Scholar speaker name
            durationSeconds = 3600, 
            // ^ Video duration (60 minutes)
            filePath = "/sdcard/videos/v2.mp4",
            // ^ Simulated local storage path
            serverUrl = "http://fake.server/v2.mp4"
            // ^ Simulated remote streaming address
        ),
        // ^ Ends second video declaration
        RelatedVideo(
        // ^ Instantiates the third RelatedVideo model
            id = "v3",
            // ^ Video ID
            title = "Science and Quran",
            // ^ Video title
            scholarName = "Dr. Zakir Naik",
            // ^ Scholar speaker name
            durationSeconds = 2400, 
            // ^ Video duration (40 minutes)
            filePath = "/sdcard/videos/v3.mp4",
            // ^ Simulated local storage path
            serverUrl = "http://fake.server/v3.mp4"
            // ^ Simulated remote streaming address
        ),
        // ^ Ends third video declaration
        RelatedVideo(
        // ^ Instantiates the fourth RelatedVideo model
            id = "v4",
            // ^ Video ID
            title = "The Names of Allah",
            // ^ Video title
            scholarName = "Sheikh Omar Suleiman",
            // ^ Scholar speaker name
            durationSeconds = 1800, 
            // ^ Video duration (30 minutes)
            filePath = "/sdcard/videos/v4.mp4",
            // ^ Simulated local storage path
            serverUrl = "http://fake.server/v4.mp4"
            // ^ Simulated remote streaming address
        ),
        // ^ Ends fourth video declaration
        RelatedVideo(
        // ^ Instantiates the fifth RelatedVideo model
            id = "v5",
            // ^ Video ID
            title = "Tafsir of Juz Amma",
            // ^ Video title
            scholarName = "Assim Al-Hakeem",
            // ^ Scholar speaker name
            durationSeconds = 2100, 
            // ^ Video duration (35 minutes)
            filePath = "/sdcard/videos/v5.mp4",
            // ^ Simulated local storage path
            serverUrl = "http://fake.server/v5.mp4"
            // ^ Simulated remote streaming address
        )
        // ^ Ends fifth video declaration
    )
    // ^ Ends list instantiation

    private val _allVideos = MutableStateFlow<List<RelatedVideo>>(fakeVideos)
    // ^ private mutable flow holding the complete master list of mock videos (starts with fakeVideos)
    
    val allVideos: StateFlow<List<RelatedVideo>> = _allVideos.asStateFlow()
    // ^ public read-only StateFlow exposed to observe the full list of videos

    private val _searchQuery = MutableStateFlow("")
    // ^ private mutable flow holding the current text entered in the search bar (starts empty)
    
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    // ^ public read-only StateFlow exposed to bind to the Search Text Field

    private val _filteredVideos = MutableStateFlow<List<RelatedVideo>>(fakeVideos)
    // ^ private mutable flow holding the filtered videos list currently shown on screen (starts with all fakeVideos)
    
    val filteredVideos: StateFlow<List<RelatedVideo>> = _filteredVideos.asStateFlow()
    // ^ public read-only StateFlow exposed to bind to the UI LazyColumn list item display

    fun onSearchQueryChanged(query: String) {
    // ^ Action function called immediately whenever the user types or deletes characters in the search text bar
        _searchQuery.value = query
        // ^ Updates the searchQuery StateFlow value with the new input
        
        if (query.isBlank()) {
        // ^ Checks if the query string is blank (only spaces or empty)
            _filteredVideos.value = _allVideos.value
            // ^ Resets the filtered list to show all videos in the master list
        } else {
        // ^ Executes if query contains search letters
            _filteredVideos.value = _allVideos.value.filter { video ->
            // ^ Filters the master list, mapping matching items to the filtered flow
                video.title.contains(query, ignoreCase = true) || 
                // ^ Checks if the video title contains the search query letters, ignoring upper/lowercase
                video.scholarName.contains(query, ignoreCase = true)
                // ^ Or checks if the scholar speaker name contains the search query letters, ignoring case
            }
            // ^ Ends filter callback block
        }
        // ^ Ends conditional check
    }
    // ^ Ends onSearchQueryChanged function
}
// ^ Ends VideoLibraryViewModel class definition
