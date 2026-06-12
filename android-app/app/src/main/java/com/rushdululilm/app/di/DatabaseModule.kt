// File: app/src/main/java/com/rushdululilm/app/di/DatabaseModule.kt
// Purpose: Hilt Module to provide Room database and DAO instances to the app
// Layer: Layer 1 — Android App (DI Layer)
// Depends on: RushdulIlmDatabase.kt, SavedAnswerDao.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS A HILT MODULE?
 * Hilt is our "smart factory." Sometimes it doesn't know how to build certain complex objects (like a database) automatically.
 * A Module is an instruction manual we give to Hilt that says: "When someone asks for a Database, here is exactly how you build it."
 * 
 * REAL-LIFE ANALOGY:
 * Think of Hilt as a smart vending machine. If you ask for an apple, it hands you one. But if you ask for a blended smoothie (the Room database), you have to give the machine a recipe module first so it knows how to make it.
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/di/
 * Why here: All Dependency Injection rules belong in the 'di' package.
 */

package com.rushdululilm.app.di
// ^ Package declaration

import android.content.Context
// ^ Imports the Android Context (used to access the app's files)
import androidx.room.Room
// ^ Imports the Room builder tool
import com.rushdululilm.app.data.local.RushdulIlmDatabase
// ^ Imports our database class
import com.rushdululilm.app.data.local.SavedAnswerDao
// ^ Imports our DAO
import dagger.Module
// ^ Imports the Module annotation
import dagger.Provides
// ^ Imports the Provides annotation for specific build instructions
import dagger.hilt.InstallIn
// ^ Imports InstallIn to tell Hilt how long this module should live
import dagger.hilt.android.qualifiers.ApplicationContext
// ^ Imports the ApplicationContext so we get the app's global context, not just one screen's context
import dagger.hilt.components.SingletonComponent
// ^ Imports SingletonComponent to say: "These items live for the entire lifetime of the app"
import javax.inject.Singleton
// ^ Imports Singleton to say: "Only ever make ONE copy of this item"

@Module
// ^ Tells Hilt: "This is an instruction manual."
@InstallIn(SingletonComponent::class)
// ^ Tells Hilt: "These instructions apply to the entire app, and the items created will live as long as the app is running."
object DatabaseModule {
// ^ Creates a singleton object to hold our instructions

    @Provides
    // ^ Tells Hilt: "Here is the instruction on how to provide a RushdulIlmDatabase."
    @Singleton
    // ^ Tells Hilt: "Only build this database ONCE and share it with everyone who asks."
    fun provideDatabase(@ApplicationContext context: Context): RushdulIlmDatabase {
    // ^ The function that actually builds the database. It needs the ApplicationContext to know where to save the file.
        return Room.databaseBuilder(
        // ^ Calls Room's built-in tool to construct a database
            context,
            // ^ Passes the context
            RushdulIlmDatabase::class.java,
            // ^ Tells Room which database blueprint to use
            "rushd_ul_ilm_db"
            // ^ Sets the actual file name of the SQLite database on the phone's storage
        ).build()
        // ^ Finally, builds and returns the database
    }
    // ^ Closes the provideDatabase function

    @Provides
    // ^ Tells Hilt: "Here is the instruction on how to provide a SavedAnswerDao."
    @Singleton
    // ^ Tells Hilt: "Only build one instance of this DAO."
    fun provideSavedAnswerDao(database: RushdulIlmDatabase): SavedAnswerDao {
    // ^ The function that provides the DAO. It needs the Database we just built above.
        return database.savedAnswerDao()
        // ^ Simply asks the database to hand over its DAO and returns it
    }
    // ^ Closes the provideSavedAnswerDao function
}
// ^ Closes the DatabaseModule object
