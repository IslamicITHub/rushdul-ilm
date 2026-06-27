// File: WhisperHelper.kt
// Purpose: Kotlin interface to communicate with the native whisper.cpp C++ code
// Layer: Layer 3 — Offline STT Pipeline
// Created: 2026-06-26 | Developer: Shaik Hidayatullah

package com.rushdululilm.app.utils

// 🏛️ CONCEPT: This class acts as our Kotlin-side bridge to the C++ code.
//    We use the 'external' keyword to tell Kotlin that the function implementation 
//    exists in the C++ library we loaded.
// 🏛️ ANALOGY: This is like a TV remote. The remote (Kotlin) has buttons (functions), 
//    but the actual electronics that do the work are inside the TV (C++ library).

class WhisperHelper {
// ^ Helper class that encapsulates whisper native calls
    
    companion object {
    // ^ Companion object is like static methods in Java, allowing us to load the library once
        init {
        // ^ init block runs when the WhisperHelper class is first loaded into memory
            System.loadLibrary("whisper_jni")
            // ^ Instructs Android to load the compiled 'libwhisper_jni.so' C++ library
        }
        // ^ Ends init block
    }
    // ^ Ends companion object
    
    external fun getSystemInfo(): String
    // ^ Declares an external function linked to Java_com_rushdululilm_app_utils_WhisperHelper_getSystemInfo in C++
    
    external fun initModel(modelPath: String): Boolean
    // ^ Declares an external function to load the model file from storage into C++ RAM
    
    external fun freeModel()
    // ^ Declares an external function to release the C++ RAM when the model is no longer needed

    
    external fun transcribeAudio(audioData: FloatArray): String
    // ^ Declares an external function that passes the normalized audio buffer to C++ and expects a transcribed string
    
}
// ^ Ends WhisperHelper class definition
