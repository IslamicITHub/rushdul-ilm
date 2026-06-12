// File: app/src/main/java/com/rushdululilm/app/data/local/SavedAnswerDao.kt
// Purpose: Data Access Object (DAO) that defines database operations for saved answers
// Layer: Layer 1 — Android App (Data Layer)
// Depends on: SavedAnswer.kt, Room library
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS A DAO?
 * DAO stands for Data Access Object. It is an interface where we list the exact database operations (like insert, delete, or read) we want to perform. Room writes the actual SQL code for us based on this interface.
 * 
 * REAL-LIFE ANALOGY:
 * Think of a DAO as a restaurant menu. You look at the menu and ask the waiter for "Insert Answer" or "Get All Answers". You don't need to know how to cook the food (write the raw SQL query); the kitchen (Room) does that for you automatically.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/data/local/
 * Why here: DAOs manage direct access to local data, so they belong in the 'data/local' folder.
 * Relates to: SavedAnswer.kt (the table it reads/writes) and AnswerHistoryRepository.kt (the class that calls these DAO functions).
 */

package com.rushdululilm.app.data.local
// ^ Package declaration

import androidx.room.Dao
// ^ Imports the annotation to tell Room this is a DAO
import androidx.room.Delete
// ^ Imports the annotation for deleting rows
import androidx.room.Insert
// ^ Imports the annotation for inserting new rows
import androidx.room.OnConflictStrategy
// ^ Imports the strategy setting for what to do if an item already exists
import androidx.room.Query
// ^ Imports the annotation for custom SQL queries
import kotlinx.coroutines.flow.Flow
// ^ Imports Flow, a continuous stream of data updates that automatically notifies the UI when the database changes

@Dao
// ^ Marks this interface as a Data Access Object for Room
interface SavedAnswerDao {
// ^ 'interface' means we only define the names of the functions; Room will write the actual code behind the scenes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // ^ Tells Room: "This function inserts a row. If one already exists with the same ID, replace it."
    suspend fun insertAnswer(savedAnswer: SavedAnswer)
    // ^ 'suspend' means this function takes time (database writing) and must be run in the background (Coroutines)

    @Delete
    // ^ Tells Room: "This function deletes a row based on the object provided."
    suspend fun deleteAnswer(savedAnswer: SavedAnswer)
    // ^ Deletes a specific saved answer from the database in the background

    @Query("SELECT * FROM saved_answers ORDER BY savedAtTimestamp DESC")
    // ^ Tells Room: "Run this exact SQL command: Get all answers from the table, ordered by time from newest to oldest."
    fun getAllAnswers(): Flow<List<SavedAnswer>>
    // ^ Returns a 'Flow' (live stream) of the list. Whenever a new answer is saved, this stream automatically sends the updated list to the UI without us having to ask again. Notice it's NOT 'suspend' because Flow runs asynchronously by itself.
    
    @Query("SELECT * FROM saved_answers WHERE id = :answerId")
    // ^ Tells Room to run a SQL command to find one specific answer matching the provided ID
    suspend fun getAnswerById(answerId: Int): SavedAnswer?
    // ^ Fetches a single answer in the background. It returns nullable (SavedAnswer?) in case the ID doesn't exist.
}
// ^ Closes the interface
