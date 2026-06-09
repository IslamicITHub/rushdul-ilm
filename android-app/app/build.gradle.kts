// File: app/build.gradle.kts (App Level)
// Purpose: The main configuration file for our app's "ingredients" and settings
// Layer: Layer 1 — Android App
// Created: 2026-05-30 | Developer: Shaik Hidayatullah

plugins {
    alias(libs.plugins.android.application) // The main tool for building Android apps
    alias(libs.plugins.kotlin.android) // The tool for writing Kotlin code
    alias(libs.plugins.kotlin.compose) // The tool for Jetpack Compose UI
    alias(libs.plugins.hilt.android) // The tool for Hilt Dependency Injection
    alias(libs.plugins.ksp) // The tool for Room database processing
}

android {
    namespace = "com.rushdululilm.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rushdululilm.app"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 🧱 CORE ANDROID & COMPOSE
    // core-ktx adds handy shortcuts to standard Android code
    implementation(libs.androidx.core.ktx)
    // lifecycle-runtime-ktx manages how screens start and stop
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // activity-compose connects normal Android screens to Compose UI
    implementation(libs.androidx.activity.compose)
    
    // 🎨 COMPOSE UI LIBRARIES
    // BOM (Bill of Materials) ensures all Compose libraries match perfectly
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Google's modern design system
    implementation(libs.androidx.material.icons.extended) // Extended icons like Mic
    
    // 🏗️ HILT (DEPENDENCY INJECTION)
    // Hilt automatically provides objects to our screens (like a smart kitchen)
    implementation(libs.hilt.android)
    // hilt-compiler is the tool that generates Hilt code behind the scenes
    ksp(libs.hilt.compiler)
    // Connects Hilt to our Navigation system
    implementation(libs.androidx.hilt.navigation.compose)

    // 💾 ROOM (LOCAL DATABASE)
    // room-runtime is the main database engine
    implementation(libs.room.runtime)
    // room-ktx adds Kotlin features (like Coroutines) to the database
    implementation(libs.room.ktx)
    // room-compiler builds the database code from our blueprints
    ksp(libs.room.compiler)

    // 🌐 NETWORKING (RETROFIT + OKHTTP)
    // Retrofit is like Python 'requests' - it talks to our FastAPI server
    implementation(libs.retrofit)
    // converter-gson turns JSON text from the server into Kotlin objects
    implementation(libs.retrofit.converter.gson)
    // OkHttp is the "engine" that Retrofit uses to send messages
    implementation(libs.okhttp)
    // logging-interceptor lets the developer see server messages in the log
    implementation(libs.okhttp.logging.interceptor)

    // 🗺️ NAVIGATION
    // This library lets us move between our 4 screens
    implementation(libs.androidx.navigation.compose)

    // 🎬 MEDIA (EXOPLAYER)
    // ExoPlayer is the powerful video player library from Google
    implementation(libs.androidx.media3.exoplayer)
    // media3-ui provides the play/pause buttons and seek bar
    implementation(libs.androidx.media3.ui)
    // media3-common shared code for media tasks
    implementation(libs.androidx.media3.common)

    // ⚡ ASYNC & BACKGROUND WORK
    // Coroutines let us do slow things (like network) without freezing the screen
    implementation(libs.kotlinx.coroutines.android)
    // WorkManager runs tasks even if the app is closed (like syncing fatwas)
    implementation(libs.androidx.work.runtime.ktx)

    // 🧠 OFFLINE AI (ONNX)
    // This lets us run the translation model directly on the phone
    implementation(libs.onnx.runtime.android)
    implementation(libs.androidx.appcompat)

    // 🧪 TESTING TOOLS
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
