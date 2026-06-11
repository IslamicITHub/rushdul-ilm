package com.rushdululilm.app.viewmodel

// File: AnswerViewModel.kt
// Purpose: The "brain" of the Answer Screen. It holds the fatwa answer and related videos.
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: MainRepository.kt, AnswerModels.kt, StateFlow
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

import androidx.lifecycle.ViewModel
// ^ Base class for ViewModels, allowing data to survive configuration changes like screen rotations
import androidx.lifecycle.viewModelScope
// ^ CoroutineScope tied to the ViewModel's lifecycle. It automatically cancels active jobs when the ViewModel is cleared.
import com.rushdululilm.app.data.repository.MainRepository
// ^ Imports our central data manager
import com.rushdululilm.app.model.FatwaAnswer
// ^ Imports the fatwa answer data class model
import com.rushdululilm.app.model.RelatedVideo
// ^ Imports the video metadata model
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Annotation that registers this class with Hilt for automated construction
import kotlinx.coroutines.flow.MutableStateFlow
// ^ Kotlin Coroutines state holder flow with write access
import kotlinx.coroutines.flow.StateFlow
// ^ Kotlin Coroutines state holder flow with read-only access
import kotlinx.coroutines.flow.asStateFlow
// ^ Converts a mutable flow to a read-only StateFlow for safe UI observation
import kotlinx.coroutines.launch
// ^ Extension function on CoroutineScope to launch a new background job asynchronously
import javax.inject.Inject
// ^ Tells Hilt how to inject constructor dependencies (like MainRepository)

// 🏛️ CONCEPT: A ViewModel manages state for a specific screen, preserving data during screen rotation.
//    We launch coroutines in viewModelScope to collect StateFlow streams from our Repository.
// 🏛️ ANALOGY: ViewModel is like a flight cockpit instrument panel. Even if the airplane turns
//    sideways (screen rotation), the dials (StateFlow properties) stay powered on and don't reset.
@HiltViewModel
// ^ Tells the Hilt compiler to generate dependency injection code for this ViewModel class
class AnswerViewModel @Inject constructor( // ^ class AnswerViewModel manages state for the AnswerScreen, constructor injected by Hilt
    private val repository: MainRepository // ^ central data repository instance for accessing fatwa answers
) : ViewModel() {
// ^ AnswerViewModel inherits from standard architecture ViewModel

    private val _currentAnswer = MutableStateFlow<FatwaAnswer?>(FatwaAnswer.PLACEHOLDER)
    // ^ private mutable flow holding the fatwa answer currently drawn on the UI (starts with placeholder)

    val currentAnswer: StateFlow<FatwaAnswer?> = _currentAnswer.asStateFlow()
    // ^ public read-only StateFlow exposed to the AnswerScreen UI

    init {
    // ^ init block runs immediately when this ViewModel class is created
        viewModelScope.launch {
        // ^ Launches a background coroutine to safely collect updates from the Repository
            repository.latestAnswer.collect { answer ->
            // ^ Collects the repository's latestAnswer flow updates
                if (answer != null) {
                // ^ Checks if a non-null answer was received from the repository
                    _currentAnswer.value = answer
                    // ^ Updates our UI state Flow value with the new answer
                }
                // ^ Ends validation check
            }
            // ^ Ends repository flow collection
        }
        // ^ Ends coroutine launch block
    }
    // ^ Ends init block

    private val _relatedVideos = MutableStateFlow<List<RelatedVideo>>(
    // ^ private mutable flow holding a list of related videos to suggest
        listOf(
        // ^ Creates a placeholder list containing mock video details
            RelatedVideo(
            // ^ Instantiates the first mock RelatedVideo object
                id = "vid_01",
                // ^ Mock video ID
                title = "Understanding Salah",
                // ^ Mock video title
                scholarName = "Fake Scholar 1",
                // ^ Mock scholar speaker
                durationSeconds = 600, 
                // ^ Mock duration (10 minutes)
                filePath = "/fake/path/1.mp4",
                // ^ Mock local downloaded filepath
                serverUrl = "http://fake.server/1.mp4"
                // ^ Mock remote video stream address
            ),
            // ^ Ends first video declaration
            RelatedVideo(
            // ^ Instantiates second mock RelatedVideo object
                id = "vid_02",
                // ^ Mock video ID
                title = "Traveling Rules",
                // ^ Mock video title
                scholarName = "Fake Scholar 2",
                // ^ Mock scholar speaker
                durationSeconds = 1200, 
                // ^ Mock duration (20 minutes)
                filePath = "/fake/path/2.mp4",
                // ^ Mock local downloaded filepath
                serverUrl = "http://fake.server/2.mp4"
                // ^ Mock remote video stream address
            )
            // ^ Ends second video declaration
        )
        // ^ Ends list of placeholder videos
    )
    // ^ Ends _relatedVideos StateFlow instantiation

    val relatedVideos: StateFlow<List<RelatedVideo>> = _relatedVideos.asStateFlow()
    // ^ public read-only StateFlow exposed to the UI for drawing related video cards

    private val _isReadingAloud = MutableStateFlow(false)
    // ^ private mutable flow tracking if the text-to-speech engine is active (defaults to false)

    val isReadingAloud: StateFlow<Boolean> = _isReadingAloud.asStateFlow()
    // ^ public read-only StateFlow for active TTS indicators

    private val _isLoading = MutableStateFlow(false)
    // ^ private mutable flow tracking loading spinner displays (defaults to false)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // ^ public read-only StateFlow for active loading states

    fun loadAnswer(answerId: String) {
    // ^ Function to fetch a specific answer detail from local DB or network API
        _isLoading.value = true
        // ^ Turns on the loading indicator state
        _currentAnswer.value = FatwaAnswer.PLACEHOLDER
        // ^ Placeholder: sets the preview answer details
        _isLoading.value = false
        // ^ Turns off the loading indicator state
        println("Loaded answer with ID: $answerId") 
        // ^ Prints a debug message in the console
    }
    // ^ Ends loadAnswer function

    fun onReadAloudPressed() {
    // ^ Function triggered when the user taps the Read Aloud button
        _isReadingAloud.value = !_isReadingAloud.value
        // ^ Toggles the true/false state of the active TTS indicator
        
        if (_isReadingAloud.value) {
        // ^ Check if text-to-speech should start
            println("TTS: Started reading aloud...")
            // ^ Prints a starting debug message
        } else {
        // ^ Runs if text-to-speech should stop
            println("TTS: Stopped reading aloud.")
            // ^ Prints a stopping debug message
        }
        // ^ Ends conditional check
    }
    // ^ Ends onReadAloudPressed function

    fun onVideoClicked(video: RelatedVideo) {
    // ^ Function triggered when the user clicks a related lecture card
        println("Video clicked: ${video.title} by ${video.scholarName}")
        // ^ Prints a debug message showing which video was clicked
    }
    // ^ Ends onVideoClicked function
}
// ^ Ends AnswerViewModel class definition
