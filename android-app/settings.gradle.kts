// File: settings.gradle.kts
// Purpose: Defines the project name and where to find libraries (repositories)
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

// The pluginManagement block tells Gradle how to find the "tools" it needs to build the app
pluginManagement {
    repositories {
        google() // Look in Google's official library store
        mavenCentral() // Look in the central library store for Java/Kotlin
        gradlePluginPortal() // Look in the store for Gradle tools
    }
}

// The dependencyResolutionManagement block tells Gradle where to find the "ingredients" (libraries) for our app
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Libraries from Google (like Jetpack Compose)
        mavenCentral() // Libraries from the community (like Retrofit)
    }
}

// This sets the name of our project in the Android Studio window
rootProject.name = "Rushd-ul-Ilm"

// This tells Gradle that we have an "app" folder that contains our actual code
include(":app")
