// File: whisper-jni.cpp
// Purpose: C++ wrapper to expose whisper.cpp functions to Kotlin
// Layer: Layer 3 — Offline STT Pipeline
// Created: 2026-06-26 | Developer: Shaik Hidayatullah

#include <jni.h>
#include <string>
#include "whisper.cpp/include/whisper.h"
#include <android/log.h>

#define TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 🏛️ CONCEPT: JNI (Java Native Interface) acts as a bridge between Kotlin and C++.
//    Kotlin cannot directly call C++ functions. We write these JNI wrapper functions 
//    that Kotlin can see, which then internally call the real whisper.cpp C++ functions.
// 🏛️ ANALOGY: JNI is like a bilingual translator standing between an English speaker (Kotlin) 
//    and a Japanese speaker (C++).

#include <vector>
// ^ Imports standard vector for dynamic arrays

// Global pointer to the whisper AI model context. 
// Kept alive across JNI calls so we don't have to load the 180MB model from disk every time we transcribe.
static whisper_context * ctx = nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_rushdululilm_app_utils_WhisperHelper_getSystemInfo(JNIEnv *env, jobject thiz) {
    const char * sys_info = whisper_print_system_info();
    return env->NewStringUTF(sys_info);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_rushdululilm_app_utils_WhisperHelper_initModel(JNIEnv *env, jobject thiz, jstring model_path_str) {
    // ^ Initializes the whisper engine using the absolute path to the .bin model file
    
    if (ctx != nullptr) {
        whisper_free(ctx);
        ctx = nullptr;
    }

    const char * model_path = env->GetStringUTFChars(model_path_str, 0);
    whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false; // We use CPU processing on Android

    ctx = whisper_init_from_file_with_params(model_path, cparams);
    // ^ Loads the 182MB model weights into RAM
    
    env->ReleaseStringUTFChars(model_path_str, model_path);

    if (ctx == nullptr) {
        LOGE("Failed to initialize whisper context from %s", model_path);
        return JNI_FALSE;
    }

    LOGI("Successfully initialized whisper context");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rushdululilm_app_utils_WhisperHelper_freeModel(JNIEnv *env, jobject thiz) {
    // ^ Frees the RAM when the app is completely closed
    if (ctx != nullptr) {
        whisper_free(ctx);
        ctx = nullptr;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_rushdululilm_app_utils_WhisperHelper_transcribeAudio(JNIEnv *env, jobject thiz, jfloatArray audio_data) {
    // ^ JNI function that receives the FloatArray from Kotlin
    
    if (ctx == nullptr) {
    // ^ Safety check
        return env->NewStringUTF("Error: Whisper model not initialized.");
    }

    jsize length = env->GetArrayLength(audio_data);
    jfloat * elements = env->GetFloatArrayElements(audio_data, 0);
    // ^ Grabs the Kotlin array data in C++ land

    std::vector<float> pcmf32(elements, elements + length);
    // ^ Copies the data into a C++ vector
    
    env->ReleaseFloatArrayElements(audio_data, elements, 0);
    // ^ Releases the JNI lock on the array

    whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    // ^ Sets up inference parameters using the Greedy strategy (fastest)
    wparams.print_progress = false;
    wparams.print_special = false;
    wparams.print_realtime = false;
    wparams.print_timestamps = false;
    wparams.language = "en";
    // ^ Force English to skip the buggy auto-detect pass which can infinite-loop on silence
    wparams.n_threads = 2;
    // ^ Run across 2 CPU cores to prevent thread thrashing/deadlocking on mobile devices
    
    // Safety optimizations for Android to prevent deadlocks:
    wparams.temperature_inc = -1.0f;
    // ^ Disables temperature fallback. (Whisper tries to re-transcribe repeatedly if confidence is low, which hangs the app)
    wparams.no_speech_thold = 0.6f;
    // ^ Increases strictness on noise/silence to prevent hallucinations ("Thank you. Thank you.")
    
    // Add progress callback to see if it's actually working or just slow
    wparams.progress_callback = [](struct whisper_context * ctx, struct whisper_state * state, int progress, void * user_data) {
        LOGI("Whisper inference progress: %d%%", progress);
    };
    wparams.progress_callback_user_data = nullptr;

    LOGI("Starting whisper_full inference on CPU...");
    if (whisper_full(ctx, wparams, pcmf32.data(), pcmf32.size()) != 0) {
    // ^ Runs the actual AI STT inference on the audio buffer
        LOGE("Failed to process audio");
        return env->NewStringUTF("Error: Transcription failed.");
    }

    const int n_segments = whisper_full_n_segments(ctx);
    std::string result = "";
    
    for (int i = 0; i < n_segments; ++i) {
    // ^ Loops through every transcribed sentence fragment
        const char * text = whisper_full_get_segment_text(ctx, i);
        result += text;
        // ^ Appends them together
    }

    LOGI("Transcription result: %s", result.c_str());
    return env->NewStringUTF(result.c_str());
    // ^ Returns the final string back to Kotlin
}
