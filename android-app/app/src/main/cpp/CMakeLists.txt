# File: CMakeLists.txt
# Purpose: Configuration file for compiling whisper.cpp into a native Android library
# Layer: Layer 3 — Offline STT Pipeline
# Created: 2026-06-26 | Developer: Shaik Hidayatullah

cmake_minimum_required(VERSION 3.22.1)

project("rushdulilm_whisper")

# Build the whisper.cpp library without building its examples or tests
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "" FORCE)

# Include the whisper.cpp repository as a subdirectory
add_subdirectory(whisper.cpp)

# Our custom JNI wrapper for the Android app
add_library(
        whisper_jni
        SHARED
        whisper-jni.cpp
)

# Find the built-in Android log library
find_library(log-lib log)

# Link our JNI wrapper against the core whisper library and Android logging
target_link_libraries(
        whisper_jni
        whisper
        ${log-lib}
)
