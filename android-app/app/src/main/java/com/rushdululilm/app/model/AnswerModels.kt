package com.rushdululilm.app.model

// File: AnswerModels.kt
// Purpose: Defines the data blueprints for an Islamic answer and related videos.
// Layer: Layer 1 — Android App (Model)
// Created: 2026-05-31 | Developer: Shaik Hidayatullah

/**
 * Represents a single Islamic answer (Fatwa) retrieved from the database.
 * Think of a "data class" like a custom form or a template. 
 * Every answer we show in the app MUST have these specific fields filled out.
 */
data class FatwaAnswer(
    // Unique identifier for this specific answer in the database
    val id: String,          
    // The original question, translated into the user's language (e.g., Telugu)
    val questionText: String, 
    // The actual fatwa answer, translated into the user's language
    val answerText: String,  
    // MANDATORY: The exact web address where this answer came from.
    // If this is missing, the rules state we cannot display the answer.
    val sourceUrl: String,   
    // The human-readable name of the website (e.g., "IslamQA.info")
    val sourceName: String,  
    // The language this specific object is translated into (e.g., "Telugu")
    val language: String,    
    // True if we got this from the phone's local storage instead of the internet
    val isOfflineCache: Boolean = false 
) {
    // A 'companion object' is like a static area inside the class.
    // We use it here to hold a fake "Placeholder" answer so we can build the UI
    // before the real backend database is connected.
    companion object {
        val PLACEHOLDER = FatwaAnswer(
            id = "test_123",
            questionText = "ప్రశ్న: ప్రయాణంలో నమాజు ఎలా చేయాలి?", // "Question: How to pray while traveling?"
            answerText = "సమాధానం: ప్రయాణంలో ఉన్నప్పుడు ఖసర్ (తగ్గించి) నమాజు చేయవచ్చు. 4 రకాతుల ఫర్జ్ నమాజును 2 రకాతులుగా చదవడం సున్నత్. " +
                         "దీనికి సంబంధించి ప్రవక్త (స) మరియు సహాబాల ఆచరణ స్పష్టంగా ఉంది.", // "Answer: While traveling, you can pray Qasr (shortened). Praying 4 rakats Fard as 2 rakats is Sunnah..."
            sourceUrl = "https://islamqa.info/en/answers/12345",
            sourceName = "IslamQA.info",
            language = "Telugu",
            isOfflineCache = false
        )
    }
}

/**
 * Represents an Islamic video lecture that relates to the fatwa answer.
 * We show these at the bottom of the Answer Screen so the user can learn more.
 */
data class RelatedVideo(
    // Unique ID for the video file
    val id: String,          
    // The title of the lecture (e.g., "Rules of Travel Prayer")
    val title: String,       
    // The name of the speaker (e.g., "Mufti Menk")
    val scholarName: String, 
    // How long the video is, in seconds
    val durationSeconds: Int,
    // The file path on the phone if the video is downloaded for offline viewing
    val filePath: String,    
    // The URL to stream the video if we have internet
    val serverUrl: String    
)
