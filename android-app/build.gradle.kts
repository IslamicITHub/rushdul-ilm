// File: build.gradle.kts (Project Level)
// Purpose: Configures tools and versions used across the entire project
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

plugins {
    // Standard Android and Kotlin tools
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    
    // Hilt tool for Dependency Injection
    alias(libs.plugins.hilt.android) apply false
    
    // KSP (Kotlin Symbol Processing) tool for Room database
    alias(libs.plugins.ksp) apply false
}
