// File: app/src/main/java/com/rushdululilm/app/data/local/FatwaSourceConverter.kt
// Purpose: Converts List of FatwaSources to JSON string and back for Room database storage
// Layer: Layer 1 — Android App (Data Layer)
// Depends on: Gson, AnswerModels.kt
// Created: 2026-06-12 | Modified: 2026-06-12
// Developer: Shaik Hidayatullah

/*
 * WHAT IS A ROOM TYPE CONVERTER?
 * Room can only save basic data types like text strings and numbers. It cannot save complex objects like a list of websites.
 * A TypeConverter acts as a translator to convert complex lists into a single text string (JSON), and vice versa.
 * 
 * REAL-LIFE ANALOGY:
 * Imagine you have a box of Lego pieces (a List), but you need to mail it in an envelope (the database cell).
 * The TypeConverter melts the Lego pieces into a flat sheet of plastic (JSON string) to fit in the envelope.
 * When you take it out, the converter molds the plastic back into the original Lego pieces (the List).
 * 
 * 📁 File Placement:
 * Goes in: app/src/main/java/com/rushdululilm/app/data/local/
 * Why here: This converter is strictly used by the Room database, so it belongs in the 'local' data folder.
 * Relates to: RushdulIlmDatabase.kt (which uses this converter) and SavedAnswer.kt (which stores the JSON string).
 */

package com.rushdululilm.app.data.local
// ^ Package declaration matching the folder

import androidx.room.TypeConverter
// ^ Imports the Room annotation to mark these translation functions
import com.google.gson.Gson
// ^ Imports the Gson library, which is Google's tool for converting Kotlin objects to JSON text
import com.google.gson.reflect.TypeToken
// ^ Imports TypeToken, which helps Gson remember the exact type of the list during conversion
import com.rushdululilm.app.model.FatwaSource
// ^ Imports our FatwaSource data class from the model folder

class FatwaSourceConverter {
// ^ Defines the converter class

    @TypeConverter
    // ^ Tells Room: "Use this function to convert data BEFORE saving it to the database"
    fun fromSourceList(sources: List<FatwaSource>): String {
    // ^ Takes a list of FatwaSources and returns a JSON text string
        return Gson().toJson(sources)
        // ^ Gson takes the list and turns it into a formatted JSON string
    }
    // ^ Closes fromSourceList function

    @TypeConverter
    // ^ Tells Room: "Use this function to convert data AFTER reading it from the database"
    fun toSourceList(sourcesJson: String): List<FatwaSource> {
    // ^ Takes a JSON text string and returns a list of FatwaSources
        val listType = object : TypeToken<List<FatwaSource>>() {}.type
        // ^ This complex line simply tells Gson: "I expect the output to be a List of FatwaSource objects"
        return Gson().fromJson(sourcesJson, listType)
        // ^ Gson reads the text string and rebuilds the actual Kotlin List
    }
    // ^ Closes toSourceList function
}
// ^ Closes the class
