// File: app/src/main/java/com/rushdululilm/app/data/local/RushdulIlmDatabase.kt
// Purpose: The main Room Database class that ties all entities and DAOs together
// Layer: Layer 1 — Android App (Data Layer)
// Depends on: SavedAnswer.kt, SavedAnswerDao.kt, FatwaSourceConverter.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS THE ROOM DATABASE CLASS?
 * This is the master entry point for the entire local SQLite database. It lists all the tables (Entities) in the database and provides access to the DAOs (the menus to interact with the tables).
 * 
 * REAL-LIFE ANALOGY:
 * If Entities are the pages of a notebook, and DAOs are the table of contents, then the Database Class is the hard cover that binds the whole notebook together.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/data/local/
 * Why here: It is the core of the local database system.
 * Relates to: DatabaseModule.kt (which creates the actual instance of this database to share across the app).
 */

package com.rushdululilm.app.data.local
// ^ Package declaration

import androidx.room.Database
// ^ Imports the Database annotation
import androidx.room.RoomDatabase
// ^ Imports the base RoomDatabase class we must inherit from
import androidx.room.TypeConverters
// ^ Imports the TypeConverters annotation

@Database(entities = [SavedAnswer::class], version = 1, exportSchema = false)
// ^ Tells Room: "This is a database. It contains the 'SavedAnswer' table. This is version 1 of our database structure. Don't worry about exporting the schema to a file right now."
@TypeConverters(FatwaSourceConverter::class)
// ^ Tells Room to use this converter class when it encounters complex data types like lists
abstract class RushdulIlmDatabase : RoomDatabase() {
// ^ This class must be 'abstract' and inherit from 'RoomDatabase()'. Room will automatically generate the actual code to implement it.

    abstract fun savedAnswerDao(): SavedAnswerDao
    // ^ Defines an abstract function that returns our DAO. Room will generate the code to connect the database to our DAO interface.
}
// ^ Closes the database class
