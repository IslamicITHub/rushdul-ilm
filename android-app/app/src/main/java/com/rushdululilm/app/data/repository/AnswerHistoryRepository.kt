// File: app/src/main/java/com/rushdululilm/app/data/repository/AnswerHistoryRepository.kt
// Purpose: Mediates between the ViewModel and the Room database for saving and retrieving answers
// Layer: Layer 1 — Android App (Data Layer)
// Depends on: SavedAnswerDao.kt, SavedAnswer.kt, FatwaAnswer.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS A REPOSITORY?
 * A Repository acts as a clean bridge between the UI/ViewModel and the messy data sources (like a database or network server). It handles the conversion of data formats.
 * 
 * REAL-LIFE ANALOGY:
 * Think of the Repository as a translator and delivery service. The UI speaks English, but the Database speaks French. The UI asks the Repository for an answer; the Repository talks to the Database in French, gets the data, translates it to English, and hands it back to the UI.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/data/repository/
 * Why here: It coordinates data access logic.
 * Relates to: SavedAnswerDao.kt (where it gets data from) and AnswersHistoryViewModel.kt (where it sends data to).
 */

package com.rushdululilm.app.data.repository
// ^ Package declaration

import com.google.gson.Gson
// ^ Imports Gson for JSON conversion
import com.rushdululilm.app.data.local.SavedAnswer
// ^ Imports the database Entity
import com.rushdululilm.app.data.local.SavedAnswerDao
// ^ Imports the database DAO
import com.rushdululilm.app.model.FatwaAnswer
// ^ Imports the UI data model
import kotlinx.coroutines.flow.Flow
// ^ Imports Flow for data streams
import javax.inject.Inject
// ^ Imports Hilt annotation for dependency injection

class AnswerHistoryRepository @Inject constructor(
// ^ '@Inject constructor' tells Hilt to automatically provide the required dependencies when creating this repository
    private val dao: SavedAnswerDao
    // ^ Hilt will automatically hand us the SavedAnswerDao from the local database
) {
// ^ Opens the class body

    fun getAllSavedAnswers(): Flow<List<SavedAnswer>> {
    // ^ Function to get a live stream of all saved answers
        return dao.getAllAnswers()
        // ^ Simply passes the Flow directly from the DAO to whoever called this function
    }
    // ^ Closes the function

    suspend fun getAnswerById(id: Int): SavedAnswer? {
    // ^ Function to fetch a specific answer by its unique ID
        return dao.getAnswerById(id)
        // ^ Calls the DAO function to get the specific answer
    }
    // ^ Closes the function

    suspend fun saveFatwaAnswer(fatwaAnswer: FatwaAnswer, language: String, isOffline: Boolean) {
    // ^ Function to take a live FatwaAnswer and save it into the Room database
        val sourcesJson = Gson().toJson(fatwaAnswer.sources)
        // ^ Converts the list of sources into a JSON string using Gson, because Room can't save lists easily
        
        val savedAnswer = SavedAnswer(
        // ^ Creates a new database row object
            questionText = fatwaAnswer.questionText,
            // ^ Copies the question
            answerText = fatwaAnswer.answerText,
            // ^ Copies the answer text
            sourcesJson = sourcesJson,
            // ^ Passes the JSON string of sources
            language = language,
            // ^ Passes the language string
            savedAtTimestamp = System.currentTimeMillis(),
            // ^ Records the current time in milliseconds
            isOfflineCache = isOffline
            // ^ Records whether this came from offline or the internet
        )
        // ^ Closes the object creation
        
        dao.insertAnswer(savedAnswer)
        // ^ Tells the DAO to insert this new row into the database
    }
    // ^ Closes the save function

    suspend fun deleteAnswer(savedAnswer: SavedAnswer) {
    // ^ Function to delete an answer from the history
        dao.deleteAnswer(savedAnswer)
        // ^ Tells the DAO to delete the row
    }
    // ^ Closes the delete function
}
// ^ Closes the class
