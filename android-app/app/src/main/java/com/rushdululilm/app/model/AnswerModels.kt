package com.rushdululilm.app.model

// File: AnswerModels.kt
// Purpose: Defines the data blueprints for an Islamic answer and related videos.
// Layer: Layer 1 — Android App (Model)
// Depends on: None (Standalone Data Blueprints)
// Created: 2026-05-31 | Modified: 2026-06-11
// Developer: Shaik Hidayatullah

// 🏛️ CONCEPT: A data class in Kotlin is a special class designed solely to store properties (data).
//    The Kotlin compiler automatically generates utility functions like toString() and copy() for data classes.
// 🏛️ ANALOGY: A data class is like an official application form. 
//    Every record gets the exact same printed fields (like ID, name, date), and we just write in the specific values for each instance.

data class FatwaAnswer(
// ^ data class defines a layout for fatwa answers received from the RAG search pipeline
    val id: String,          
    // ^ val declares a read-only string field for the unique database ID of this fatwa
    val questionText: String, 
    // ^ val declares a read-only string field for the user's original question text
    val answerText: String,  
    // ^ val declares a read-only string field for the generated Islamic answer text (in the user's language)
    val sources: List<FatwaSource>,   
    // ^ val declares a read-only list of FatwaSource objects containing citations for this answer
    val language: String,    
    // ^ val declares a read-only string field representing the translation language name (e.g. "Urdu")
    val isOfflineCache: Boolean = false,
    // ^ val declares a read-only boolean indicating if this answer was retrieved from local room cache (defaults to false)
    val expandedQuery: String? = null
    // ^ val declares a nullable read-only string containing the technical search terms expanded by the RAG model
) {
// ^ Starts FatwaAnswer body
    companion object {
    // ^ companion object stores static variables and default placeholders
        val PLACEHOLDER = FatwaAnswer(
        // ^ PLACEHOLDER is a hardcoded FatwaAnswer instance used to preview UI components inside Android Studio
            id = "test_123",
            // ^ Sets placeholder ID to "test_123"
            questionText = "ప్రశ్న: ప్రయాణంలో నమాజు ఎలా చేయాలి?", 
            // ^ Sets Telugu placeholder question "How to pray while traveling?"
            answerText = "సమాధానం: ప్రయాణంలో ఉన్నప్పుడు ఖసర్ (తగ్గించి) నమాజు చేయవచ్చు. 4 రకాతుల ఫర్జ్ నమాజును 2 రకాతులుగా చదవడం సున్నత్. " +
                         "దీనికి సంబంధించి ప్రవక్త (స) మరియు సహాబాల ఆచరణ స్పష్టంగా ఉంది.",
            // ^ Sets Telugu placeholder answer text outlining the traveler's shortened prayer (Qasr) rules
            sources = listOf(
            // ^ Sets a list of placeholder sources
                FatwaSource("IslamQA.info", "https://islamqa.info/en/answers/12345"),
                // ^ Adds IslamQA.info placeholder citation source
                FatwaSource("Darul Ifta Deoband", "https://darulifta-deoband.com/en/12345")
                // ^ Adds Darul Ifta Deoband placeholder citation source
            ),
            // ^ Ends sources list
            language = "Telugu",
            // ^ Sets placeholder language value to "Telugu"
            isOfflineCache = false,
            // ^ Sets offline indicator to false
            expandedQuery = "Detailed rulings, scenarios and exceptions for travel prayer (Musafir Salah, Qasr) in Islamic Fiqh."
            // ^ Sets placeholder expanded search query
        )
        // ^ Ends PLACEHOLDER configuration
    }
    // ^ Ends companion object block
}
// ^ Ends FatwaAnswer class definition

data class FatwaSource(
// ^ data class representing a single cited web reference website
    val name: String,
    // ^ val declares a read-only string for the website display name (e.g., "IslamQA.info")
    val url: String
    // ^ val declares a read-only string for the direct hyperlink to the original fatwa web page
)
// ^ Ends FatwaSource class definition

data class RelatedVideo(
// ^ data class containing metadata for video lectures that explain the fatwa topic
    val id: String,          
    // ^ val declares a read-only string for the unique video identifier
    val title: String,       
    // ^ val declares a read-only string for the video title shown on the screen card
    val scholarName: String, 
    // ^ val declares a read-only string for the speaker scholar's name
    val durationSeconds: Int,
    // ^ val declares a read-only integer representing total duration in seconds
    val filePath: String,    
    // ^ val declares a read-only string specifying the local filesystem path on the device (if downloaded)
    val serverUrl: String    
    // ^ val declares a read-only string for the remote stream server URL address
)
// ^ Ends RelatedVideo class definition
