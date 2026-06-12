// File: app/src/main/java/com/rushdululilm/app/viewmodel/AnswersHistoryViewModel.kt
// Purpose: Manages the state of the Answers History screen by fetching data from the local DB
// Layer: Layer 1 — Android App (ViewModel Layer)
// Depends on: AnswerHistoryRepository.kt, SavedAnswer.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS A VIEWMODEL?
 * The ViewModel is the "brain" for a specific screen. It fetches data and holds onto it (state) so the screen can just display it without doing any heavy lifting.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/viewmodel/
 * Why here: All UI "brains" live in the viewmodel package.
 */

package com.rushdululilm.app.viewmodel
// ^ Package declaration

import androidx.lifecycle.ViewModel
// ^ Imports the base ViewModel class
import androidx.lifecycle.viewModelScope
// ^ Imports viewModelScope, which safely ties background work to the life of the ViewModel
import com.rushdululilm.app.data.local.SavedAnswer
// ^ Imports the SavedAnswer database entity
import com.rushdululilm.app.data.repository.AnswerHistoryRepository
// ^ Imports the repository
import dagger.hilt.android.lifecycle.HiltViewModel
// ^ Imports Hilt annotation to tell the app this is a ViewModel that needs dependencies
import kotlinx.coroutines.flow.SharingStarted
// ^ Imports SharingStarted rule for converting Flows to StateFlows
import kotlinx.coroutines.flow.StateFlow
// ^ Imports StateFlow for holding data
import kotlinx.coroutines.flow.stateIn
// ^ Imports stateIn to convert a live Flow into a UI-friendly StateFlow
import kotlinx.coroutines.launch
// ^ Imports launch to start background tasks
import javax.inject.Inject
// ^ Imports Inject annotation

@HiltViewModel
// ^ Tells Hilt: "I am a ViewModel, please build me and hand me my requested items."
class AnswersHistoryViewModel @Inject constructor(
// ^ The constructor asks for the repository
    private val repository: AnswerHistoryRepository
    // ^ Hilt automatically provides the AnswerHistoryRepository
) : ViewModel() {
// ^ Inherits from ViewModel

    val savedAnswers: StateFlow<List<SavedAnswer>> = repository.getAllSavedAnswers()
    // ^ Asks the repository for the live stream of answers...
        .stateIn(
        // ^ ...and converts it into a StateFlow so the UI can easily observe it
            scope = viewModelScope,
            // ^ Ties this flow to the ViewModel's lifespan
            started = SharingStarted.WhileSubscribed(5000),
            // ^ Keeps the stream active for 5 seconds after the screen disappears, saving memory
            initialValue = emptyList()
            // ^ Starts with an empty list until the real data loads from the database
        )
        // ^ Ends stateIn block

    fun deleteAnswer(savedAnswer: SavedAnswer) {
    // ^ Function called when the user wants to delete an answer
        viewModelScope.launch {
        // ^ Launches a background coroutine so the UI doesn't freeze
            repository.deleteAnswer(savedAnswer)
            // ^ Tells the repository to delete the answer from the database
        }
        // ^ Closes the coroutine block
    }
    // ^ Closes deleteAnswer function
}
// ^ Closes class
