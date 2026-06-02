package com.rushdululilm.app.viewmodel

// File: AnswerViewModel.kt
// Purpose: The "brain" of the Answer Screen. It holds the fatwa answer and related videos.
// Layer: Layer 1 — Android App (ViewModel)
// Depends on: AnswerModels.kt
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

import androidx.lifecycle.ViewModel
import com.rushdululilm.app.model.FatwaAnswer
import com.rushdululilm.app.model.RelatedVideo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * The ViewModel for the Answer Screen.
 * 
 * @HiltViewModel tells Hilt to automatically construct this class for us when the screen needs it.
 * @Inject constructor() tells Hilt how to build it.
 */
@HiltViewModel
class AnswerViewModel @Inject constructor() : ViewModel() {

    // --- StateFlow Properties ---
    // StateFlow acts as a "live variable". When its value updates, the UI automatically redraws.

    // Holds the currently displayed fatwa answer. It starts as null, but we'll load the placeholder.
    private val _currentAnswer = MutableStateFlow<FatwaAnswer?>(FatwaAnswer.PLACEHOLDER)
    val currentAnswer: StateFlow<FatwaAnswer?> = _currentAnswer.asStateFlow()

    // Holds a list of related video lectures to show at the bottom.
    // For now, we populate it with 2 fake (placeholder) videos.
    private val _relatedVideos = MutableStateFlow<List<RelatedVideo>>(
        listOf(
            RelatedVideo(
                id = "vid_01",
                title = "Understanding Salah",
                scholarName = "Fake Scholar 1",
                durationSeconds = 600, // 10 minutes
                filePath = "/fake/path/1.mp4",
                serverUrl = "http://fake.server/1.mp4"
            ),
            RelatedVideo(
                id = "vid_02",
                title = "Traveling Rules",
                scholarName = "Fake Scholar 2",
                durationSeconds = 1200, // 20 minutes
                filePath = "/fake/path/2.mp4",
                serverUrl = "http://fake.server/2.mp4"
            )
        )
    )
    val relatedVideos: StateFlow<List<RelatedVideo>> = _relatedVideos.asStateFlow()

    // Tracks if the Text-To-Speech (TTS) engine is currently reading the answer out loud.
    private val _isReadingAloud = MutableStateFlow(false)
    val isReadingAloud: StateFlow<Boolean> = _isReadingAloud.asStateFlow()

    // Tracks if we are currently waiting for the server/database to fetch the answer.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- User Actions ---

    /**
     * Called when the screen first opens to load a specific answer.
     * @param answerId The unique ID of the answer we want to fetch.
     */
    fun loadAnswer(answerId: String) {
        // Placeholder logic: For now, we just ensure the PLACEHOLDER is loaded.
        // In the future, this will ask the database or server for the real answer.
        _isLoading.value = true
        _currentAnswer.value = FatwaAnswer.PLACEHOLDER
        _isLoading.value = false
        println("Loaded answer with ID: $answerId") // Placeholder log
    }

    /**
     * Called when the user taps the "Read Aloud" button.
     */
    fun onReadAloudPressed() {
        // Placeholder logic: Toggle the visual state and print a message.
        // Later, this will trigger the Android TTS or Coqui XTTS service.
        _isReadingAloud.value = !_isReadingAloud.value
        
        if (_isReadingAloud.value) {
            println("TTS: Started reading aloud...")
        } else {
            println("TTS: Stopped reading aloud.")
        }
    }

    /**
     * Called when the user taps one of the related video cards.
     * @param video The video that was clicked.
     */
    fun onVideoClicked(video: RelatedVideo) {
        // Placeholder logic: Print which video was clicked.
        // Later, this will navigate to the Video Player screen.
        println("Video clicked: ${video.title} by ${video.scholarName}")
    }
}
