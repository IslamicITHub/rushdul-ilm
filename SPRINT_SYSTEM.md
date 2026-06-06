# SPRINT_SYSTEM.md
# Rushd-ul-Ilm (رشد العلم) — Sprint & Micro-Task Breakdown System
# Developer: Shaik Hidayatullah, Kurnool, Andhra Pradesh, India
# Version: 1.0 | Created: 2026-05-28
#
# ════════════════════════════════════════════════════════════════════
# WHY THIS FILE EXISTS
#
# Giving an AI agent a huge task like "Build Phase 1" is like asking a
# carpenter to "build a house" with no blueprints. The agent will either
# hallucinate details, skip things, or produce code you don't understand.
#
# This file breaks every Phase into:
#   Phase → Sprint → Sub-sprint → Micro-task
#
# Each MICRO-TASK is designed to:
#   - Fit inside ONE AI agent session (20-40 minutes of coding)
#   - Produce ONE testable, verifiable output
#   - Be completely self-contained — the agent does not need to "remember" context
#   - Have a clear DONE condition — you know exactly when it is finished
#
# HOW AI AGENTS USE THIS FILE:
#   1. Read ACTIVITY_LOG.md to find CURRENT_SPRINT and CURRENT_MICRO_TASK
#   2. Find that micro-task in this file
#   3. Do ONLY that micro-task — nothing more
#   4. Verify the DONE condition is met
#   5. Update ACTIVITY_LOG.md with the next micro-task
#   6. Stop and tell the developer: "Micro-task [ID] complete. Ready for [NEXT_ID]."
#
# MICRO-TASK ID FORMAT:
#   P1.S1.SS1.MT1
#   │  │  │   └── Micro-task number
#   │  │  └────── Sub-sprint number
#   │  └───────── Sprint number
#   └──────────── Phase number
# ════════════════════════════════════════════════════════════════════

---

## 📊 SPRINT PROGRESS TRACKER (Update this dashboard every session)

```
CURRENT_PHASE:        3
CURRENT_SPRINT:       3.1
CURRENT_SUB_SPRINT:   3.1.4
CURRENT_MICRO_TASK:   P3.S1.SS1.MT5   ← START HERE

PHASE 1 PROGRESS:
  Sprint 1.1 — Environment & Project Setup     [x] 7/7 micro-tasks done
  Sprint 1.2 — App Structure & Navigation      [x] 6/6 micro-tasks done
  Sprint 1.3 — Home Screen                     [x] 6/8 micro-tasks done
  Sprint 1.4 — Answer Screen                   [x] 3/7 micro-tasks done
  Sprint 1.5 — Video Library Screen            [x] 2/5 micro-tasks done
  Sprint 1.6 — Settings Screen                 [x] 1/5 micro-tasks done
  Sprint 1.7 — String Resources & Theming      [x] 2/2 micro-tasks done
  Sprint 1.8 — Phase 1 Integration Test        [x] 2/2 micro-tasks done

PHASE 2 PROGRESS:
  Sprint 2.1 — Install Docker & NVIDIA Toolkit [x] 2/2 micro-tasks done
  Sprint 2.2 — Create docker-compose.yml       [x] 1/1 micro-tasks done
  Sprint 2.3 — Ollama + Qwen3:4b Setup        [x] 1/1 micro-tasks done
  Sprint 2.4 — FastAPI Server Skeleton         [x] 2/2 micro-tasks done
  Sprint 2.5 — Qdrant Vector DB Setup         [x] 1/1 micro-tasks done
  Sprint 2.6 — Wire FastAPI to Ollama         [x] 1/1 micro-tasks done
  Sprint 2.7 — Phase 2 Integration Test       [x] 2/2 micro-tasks done
PHASE 3 PROGRESS:
  Sprint 3.1 — Scraper Review & Test          [x] 3/3 micro-tasks done

---

## PHASE 3 — KNOWLEDGE INGESTION
**Goal:** Process raw fatwa data into vector embeddings and store them in Qdrant for semantic search.

##### Micro-task P3.S1.SS1.MT3 — Embed islamqa.info into Qdrant
```
TASK:   Create an ingestion script to convert fatwas into vector embeddings and store them in Qdrant.

WHY:    Standard keyword search (like Ctrl+F) only finds exact words.
        Vector embeddings allow "semantic search" — finding answers by meaning.
        We'll use the "paraphrase-multilingual-mpnet-base-v2" model because it
        understands 50+ languages, including English, Telugu, and Urdu.

STEP BY STEP:
  1. Add 'qdrant-client' and 'sentence-transformers' to backend/requirements.txt.
  2. Create backend/ingest_islamqa.py.
  3. In the script:
     - Connect to the SQLite database created in MT2.
     - Initialize the Qdrant client and create a collection named "islamqa".
     - Load the embedding model (this will download ~420MB on first run).
     - Process fatwas in batches (e.g., 100 at a time).
     - For each fatwa, combine 'title' and 'question' as the text to embed.
     - Store the vector + metadata (id, title, source_url) in Qdrant.
  4. Run the script and verify vectors are stored in Qdrant.

DONE CONDITION:
  Qdrant dashboard (http://localhost:6333/dashboard) shows the "islamqa" collection.
  The collection contains points (vectors) with metadata.
  A test query in the dashboard returns relevant fatwa IDs.

##### Micro-task P3.S1.SS1.MT4 — Build LlamaIndex RAG pipeline
```
TASK:   Create the core RAG logic using LlamaIndex to connect Qdrant and Ollama.

WHY:    RAG (Retrieval-Augmented Generation) ensures the AI only answers using 
        our approved Islamic database. LlamaIndex acts as the "orchestrator" 
        that finds the right fatwas and hands them to the AI to summarize.

STEP BY STEP:
  1. Update backend/requirements.txt and backend/Dockerfile with llama-index.
  2. Create backend/rag_pipeline.py.
  3. Implement RagPipeline class:
     - Initialize QdrantVectorStore and HuggingFaceEmbedding.
     - Initialize Ollama LLM (qwen3:4b) with context_window=4096.
     - Implement ask() method with a strict Islamic system prompt.
  4. Integrate RagPipeline into backend/fastapi_server.py.
  5. Test with curl: verify answer text + source URLs are returned.

DONE CONDITION:
  'curl -X POST http://localhost:8000/query' returns a JSON response 
  containing "answer" and "sources" (list of URLs).

NEXT MICRO-TASK: P3.S1.SS1.MT5
```

##### Micro-task P3.S1.SS1.MT5 — Ingest Deoband data + test source filter
```
TASK:   Embed the Darul Ifta Deoband SQLite database into Qdrant.

WHY:    We want to support multiple Islamic schools (Madhabs). 
        By adding Deoband data, we can later let the user filter 
        answers to only see Hanafi fatwas if they prefer.

STEP BY STEP:
  1. Create backend/ingest_deoband.py (similar to ingest_islamqa.py).
  2. Map the Deoband SQLite fields (fatwa_id, question, answer, url) to Qdrant payload.
  3. Run the ingestion and verify points are added to a "deoband" collection.
  4. Update RagPipeline to support querying specific collections or all.

DONE CONDITION:
  Qdrant shows a new collection "deoband" with points.
  A query to the backend can return results from both sources.

NEXT MICRO-TASK: P3.S1.SS1.MT6
```
## Goal: A working Android app with 4 screens, fake data,
##       no real backend connection yet.
## ══════════════════════════════════════════════════════

---

### SPRINT 1.1 — Environment & Project Setup
**Goal:** Android Studio installed, new project created, dependencies added, emulator running.
**Why first:** Everything else depends on having a working project. Don't write any screens until this sprint is 100% done.

---

#### Sub-sprint 1.1.1 — Install Android Studio on Parrot OS Linux

##### Micro-task P1.S1.SS1.MT1 — Install Android Studio
```
TASK:   Install Android Studio on Parrot OS Linux
METHOD: Download from https://developer.android.com/studio
        Extract and run: ./android-studio/bin/studio.sh
        OR: sudo snap install android-studio --classic

STEP BY STEP:
  1. Open terminal in Parrot OS
  2. Run: sudo snap install android-studio --classic
  3. Launch: android-studio
  4. Complete the Setup Wizard (accept defaults — choose "Standard" install)
  5. Let it download Android SDK (this takes 10-20 minutes)

DONE CONDITION:
  Android Studio opens without errors.
  You can see the "Welcome to Android Studio" screen.
  The SDK is downloaded (shown in SDK Manager — Tools → SDK Manager).

VERIFY WITH:
  android-studio → File → Settings → Android SDK
  Should show: Android 8.0 (API 26) or higher is installed (green checkbox)

NEXT MICRO-TASK: P1.S1.SS1.MT2
```

##### Micro-task P1.S1.SS1.MT2 — Enable KVM for Android Emulator
```
TASK:   Enable KVM acceleration so the Android emulator runs fast on Parrot OS

WHY:    Without KVM, the Android emulator runs very slowly on Linux.
        KVM (Kernel-based Virtual Machine) lets the emulator use your CPU's
        hardware virtualization — similar to how VirtualBox uses hardware acceleration.

STEP BY STEP:
  1. Open terminal
  2. Check if KVM is already enabled: ls /dev/kvm
     - If you see /dev/kvm → KVM is ready, skip to step 5
     - If "No such file" → continue to step 3
  3. Install KVM: sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients
  4. Add your user to the kvm group: sudo usermod -aG kvm $USER
  5. Reboot Parrot OS: sudo reboot
  6. After reboot, verify: ls -la /dev/kvm
     Should show: crw-rw---- 1 root kvm ...

DONE CONDITION:
  Terminal shows /dev/kvm exists after reboot.
  Command 'kvm-ok' (install with: sudo apt install cpu-checker) shows:
  "INFO: /dev/kvm exists. KVM acceleration can be used."

NEXT MICRO-TASK: P1.S1.SS1.MT3
```

##### Micro-task P1.S1.SS1.MT3 — Create Android Virtual Device (Emulator)
```
TASK:   Create an Android emulator (virtual phone) inside Android Studio

WHY:    We need a virtual phone to test the app without a real phone.
        We'll use a Pixel 5 emulator running Android 12 for testing.

STEP BY STEP:
  1. Open Android Studio
  2. Click: Tools → Device Manager
  3. Click: "Create Device" button
  4. Choose phone: Select "Pixel 5" → Next
  5. Choose Android version: Select "API 31 (Android 12)" → Download if needed → Next
  6. AVD Name: leave as default "Pixel_5_API_31"
  7. Click Finish
  8. Click the ▶ Play button next to the new device to start the emulator
  9. Wait for the Android home screen to appear (may take 2-3 minutes first time)

DONE CONDITION:
  The Android emulator window opens and shows the Android home screen.
  You can swipe and interact with it like a real phone.

NEXT MICRO-TASK: P1.S1.SS2.MT1
```

---

#### Sub-sprint 1.1.2 — Create the Android Studio Project

##### Micro-task P1.S1.SS2.MT1 — Create New Android Project
```
TASK:   Create the Rushd-ul-Ilm Android project in Android Studio

CRITICAL: Package name MUST be exactly: com.rushdululilm.app — never change this

STEP BY STEP:
  1. In Android Studio: File → New → New Project
  2. Choose template: "Empty Activity" → Next
  3. Fill in the form EXACTLY as follows:
     Name:           Rushd-ul-Ilm
     Package name:   com.rushdululilm.app
     Save location:  /home/hidayat/Documents/Islamic Knowledge Q&A App/android-app/
     Language:       Kotlin
     Minimum SDK:    API 26 ("Android 8.0 Oreo") — covers 95% of Indian phones
     Build config:   Kotlin DSL (build.gradle.kts)  ← important: NOT Groovy DSL
  4. Click Finish
  5. Wait for Gradle to sync (progress bar at bottom — may take 5-10 minutes first time)
  6. When done: File → Save All (Ctrl+S)

DONE CONDITION:
  Project opens in Android Studio without errors.
  The file tree on the left shows:
    app/
      src/
        main/
          java/com/rushdululilm/app/
            MainActivity.kt
          res/
            layout/
            values/
  Gradle sync shows "BUILD SUCCESSFUL" in the Build tab (bottom of screen).

NEXT MICRO-TASK: P1.S1.SS2.MT2
```

##### Micro-task P1.S1.SS2.MT2 — Add Dependencies to build.gradle.kts
```
TASK:   Add all required Android libraries to the app's build.gradle.kts file

WHY:    build.gradle.kts is the "shopping list" of libraries our app uses.
        Like pip install in Python, Gradle downloads these libraries automatically.
        We add everything now so we never have to add a library mid-feature.

AI AGENT INSTRUCTION:
  Open: android-app/app/build.gradle.kts
  Replace the entire contents with the heavily-commented version below.
  Write every dependency with a comment explaining what it is and why we need it.

DEPENDENCIES TO ADD (with comments):
  - Jetpack Compose BOM (latest) — manages all Compose library versions
  - Compose UI + Material3 — UI components and Google's design system
  - Compose ViewModel + Runtime — MVVM ViewModel integration with Compose
  - Hilt (DI) — dependency injection — hilt-android + hilt-compiler
  - Room (SQLite) — local database — room-runtime + room-ktx + room-compiler + room-testing
  - Retrofit + OkHttp — HTTP client for calling FastAPI server
  - Gson converter — converts JSON responses to Kotlin data classes
  - ExoPlayer / Media3 — video player
  - Navigation Compose — handles navigation between screens
  - Coroutines — for async operations (network calls, database)
  - WorkManager — background sync of offline databases
  - ONNX Runtime Android 1.17.3 — runs Opus-MT offline translation on phone

DONE CONDITION:
  Gradle syncs without errors after saving the file.
  "BUILD SUCCESSFUL" shown in the Build tab.
  No red squiggly lines in build.gradle.kts.

NEXT MICRO-TASK: P1.S1.SS2.MT3
```

##### Micro-task P1.S1.SS2.MT3 — Configure Hilt in Application Class
```
TASK:   Set up Hilt Dependency Injection — create the Application class

WHY HILT EXISTS (explain this before any code):
  In Android, many classes need the same objects — for example, every screen
  might need the database, the network client, and the ViewModel.
  Without Hilt, you'd have to manually create and pass these objects everywhere.
  Hilt is like a smart factory — you tell it what objects exist once,
  and it automatically provides them wherever they're needed.
  This is called "Dependency Injection" (DI).

FILES TO CREATE:
  1. android-app/app/src/main/java/com/rushdululilm/app/RushdulIlmApplication.kt
     - Add @HiltAndroidApp annotation on the Application class
     - Explain what @HiltAndroidApp does in a comment

  2. Modify AndroidManifest.xml:
     - Add android:name=".RushdulIlmApplication" to the <application> tag
     - Explain why this is needed in a comment

DONE CONDITION:
  The app compiles without errors.
  Run the app on emulator — it should show a blank "Hello Android" screen.
  No Hilt-related errors in Logcat.

NEXT MICRO-TASK: P1.S1.SS2.MT4 (run app for first time)
```

##### Micro-task P1.S1.SS2.MT4 — Run the App for the First Time
```
TASK:   Run the app on the emulator to verify the project setup is correct

STEP BY STEP:
  1. In Android Studio, at the top toolbar: make sure the emulator is selected
     (dropdown shows "Pixel_5_API_31" or similar)
  2. Click the green ▶ Run button (or press Shift+F10)
  3. Wait for the build to complete (progress at bottom)
  4. The emulator should open the app and show a screen with "Hello Android!" text

DONE CONDITION:
  App runs on the emulator without crashing.
  "Hello Android!" (or similar default Compose text) is visible on screen.
  Logcat (bottom panel) shows no ERROR or FATAL lines.

SPRINT 1.1 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.2 — App Structure & Navigation
NEXT MICRO-TASK: P1.S2.SS1.MT1
```

---

### SPRINT 1.2 — App Structure & Navigation
**Goal:** All 4 screens exist as empty placeholders. Tapping a button navigates between screens. Nothing crashes.

---

#### Sub-sprint 1.2.1 — Create Package Folder Structure

##### Micro-task P1.S2.SS1.MT1 — Create All Package Folders
```
TASK:   Create the folder structure inside com.rushdululilm.app

WHY:    Like rooms in a house, each folder has a specific purpose.
        Organizing code into folders from day one prevents chaos later.
        This pattern (ui/viewmodel/data/di/utils) is the industry standard
        for Android MVVM architecture.

FOLDERS TO CREATE (right-click package → New → Package):
  com.rushdululilm.app.ui.screens      ← Jetpack Compose screen files
  com.rushdululilm.app.ui.components   ← Reusable UI pieces (buttons, cards)
  com.rushdululilm.app.ui.theme        ← Colors, fonts, Material3 theme
  com.rushdululilm.app.viewmodel       ← MVVM ViewModels for each screen
  com.rushdululilm.app.data.local      ← Room database entities and DAOs
  com.rushdululilm.app.data.remote     ← Retrofit API interface + response models
  com.rushdululilm.app.data.repository ← Repository layer (connects ViewModel to data)
  com.rushdululilm.app.di              ← Hilt dependency injection modules
  com.rushdululilm.app.utils           ← Helper functions (network check, audio, etc.)
  com.rushdululilm.app.model           ← Data classes used across the app

AI AGENT INSTRUCTION:
  After creating folders, create one placeholder .kt file in each folder:
  - ui/screens/ → create a file called Screens.kt with a comment explaining the folder
  - viewmodel/ → create ViewModels.kt with a comment
  - etc.
  This prevents Android Studio from hiding empty packages.

DONE CONDITION:
  All 10 packages visible in the Project tree on the left.
  No compilation errors.

NEXT MICRO-TASK: P1.S2.SS1.MT2
```

##### Micro-task P1.S2.SS1.MT2 — Create Route Constants File
```
TASK:   Create a file that defines all navigation route strings

WHY:    In Jetpack Compose Navigation, every screen has a "route" — a text string
        that works like a street address. When you navigate to HomeScreen, you tell
        the navigation system "go to the 'home' route".
        Putting all route strings in one file prevents typos and makes it easy
        to change a route name later without hunting through multiple files.

FILE TO CREATE:
  Path: com/rushdululilm/app/ui/screens/Routes.kt

CONTENT (write with full comments):
  object Routes {
      const val HOME = "home"           // Main screen with mic button
      const val ANSWER = "answer"       // Shows the fatwa answer + source URL
      const val VIDEO_LIBRARY = "video_library"  // Searchable video list
      const val SETTINGS = "settings"   // App preferences and offline DB download
  }

DONE CONDITION:
  Routes.kt compiles without errors.
  All 4 route constants are defined and commented.

NEXT MICRO-TASK: P1.S2.SS2.MT1
```

---

#### Sub-sprint 1.2.2 — Build the Navigation Graph

##### Micro-task P1.S2.SS2.MT1 — Create 4 Empty Placeholder Screens
```
TASK:   Create 4 minimal Jetpack Compose screen files — empty placeholders only

WHY JETPACK COMPOSE (explain before any code):
  Traditional Android used XML files to describe the UI (like HTML for Android).
  Jetpack Compose is newer and lets you write UI directly in Kotlin code.
  A @Composable function is a function that draws something on screen.
  When its data changes, Compose automatically redraws it — like a live webpage.

FILES TO CREATE:

  1. ui/screens/HomeScreen.kt
     - @Composable fun HomeScreen(navController: NavController)
     - Shows: Text("Home Screen — Mic Button Goes Here", fontSize=20.sp)
     - Comment: "Placeholder — mic button will be added in Sprint 1.3"

  2. ui/screens/AnswerScreen.kt
     - @Composable fun AnswerScreen(navController: NavController)
     - Shows: Text("Answer Screen — Fatwa Answer Goes Here", fontSize=20.sp)
     - Comment: "Placeholder — answer display will be added in Sprint 1.4"

  3. ui/screens/VideoLibraryScreen.kt
     - @Composable fun VideoLibraryScreen(navController: NavController)
     - Shows: Text("Video Library — Islamic Lectures Go Here", fontSize=20.sp)
     - Comment: "Placeholder — video list will be added in Sprint 1.5"

  4. ui/screens/SettingsScreen.kt
     - @Composable fun SettingsScreen(navController: NavController)
     - Shows: Text("Settings Screen — Preferences Go Here", fontSize=20.sp)
     - Comment: "Placeholder — settings UI will be added in Sprint 1.6"

DONE CONDITION:
  All 4 files compile without errors.
  Each file has a header comment block (see CODE QUALITY RULES in AGENT_RULES.md).

NEXT MICRO-TASK: P1.S2.SS2.MT2
```

##### Micro-task P1.S2.SS2.MT2 — Create NavGraph.kt (Navigation Controller)
```
TASK:   Create the navigation graph that connects all 4 screens

WHY NAVIGATION EXISTS (explain before code):
  An Android app with multiple screens needs a "traffic controller" — something
  that knows which screen to show and how to move between them.
  NavHost is like a photo frame — it shows one screen at a time.
  NavController is the remote control — it changes which screen the frame shows.
  NavGraph defines all available screens and how they connect.

FILE TO CREATE:
  Path: com/rushdululilm/app/ui/screens/NavGraph.kt

CONTENT REQUIREMENTS:
  - Create a @Composable function: fun RushdulIlmNavGraph()
  - Use rememberNavController() to create the navigation controller
  - Use NavHost with startDestination = Routes.HOME
  - Add composable() entries for all 4 routes: HOME, ANSWER, VIDEO_LIBRARY, SETTINGS
  - Each composable() block calls the corresponding screen function
  - Add a bottom navigation bar with 4 icons:
    * Home (mic icon)
    * Video Library (video icon)
    * Settings (settings icon)
    * [Answer screen is navigated to programmatically — no bottom nav tab for it]
  - Every line commented

DONE CONDITION:
  NavGraph.kt compiles without errors.
  Each route is defined and linked to the correct screen Composable.

NEXT MICRO-TASK: P1.S2.SS2.MT3
```

##### Micro-task P1.S2.SS2.MT3 — Wire NavGraph into MainActivity & Test Navigation
```
TASK:   Replace the default MainActivity content with RushdulIlmNavGraph()
        Then run the app and verify you can navigate between all 4 screens.

FILE TO MODIFY:
  Path: com/rushdululilm/app/MainActivity.kt

CHANGES:
  - Add @AndroidEntryPoint annotation (required for Hilt in Activities)
  - Inside setContent { }, replace the default Hello World composable
    with: RushdulIlmNavGraph()
  - Add a comment explaining why @AndroidEntryPoint is needed

TEST PROCEDURE:
  1. Run the app on the emulator
  2. The Home screen placeholder should appear
  3. Tap bottom nav icons — Video Library and Settings placeholders should appear
  4. Back button should navigate back correctly

DONE CONDITION:
  All 3 bottom nav tabs show their placeholder screens.
  No crashes when navigating.
  Back button works correctly.
  Logcat shows no ERROR lines during navigation.

SPRINT 1.2 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.3 — Home Screen
NEXT MICRO-TASK: P1.S3.SS1.MT1
```

---

### SPRINT 1.3 — Home Screen
**Goal:** The Home screen shows a large mic button, language selector, and source selector with real-looking UI. All fake/placeholder data. No real recording yet.

---

#### Sub-sprint 1.3.1 — App Theme & Colors

##### Micro-task P1.S3.SS1.MT1 — Create App Theme (Colors & Typography)
```
TASK:   Define the app's color palette and typography in Material3 theme

WHY THEMING FIRST:
  Like choosing paint colors before building walls — define colors once,
  use them everywhere. This prevents inconsistent colors across screens.
  If you hardcode colors in each screen, changing them later means editing 50 files.

FILES TO CREATE/MODIFY:
  1. ui/theme/Color.kt — define all named colors:
     - IslamicGreen = Color(0xFF2E7D32)    ← Islamic answers, halal
     - QuranicBlue = Color(0xFF1565C0)     ← Quranic references
     - OfflineOrange = Color(0xFFE65100)   ← Offline mode indicator
     - ErrorRed = Color(0xFFB71C1C)        ← Errors only
     - BackgroundCream = Color(0xFFFFF8E1) ← Warm off-white background
     - TextPrimary = Color(0xFF1A1A1A)     ← Main text color

  2. ui/theme/Type.kt — define text styles:
     - bodyLarge: fontSize=18.sp (minimum 16sp — accessibility rule)
     - bodyMedium: fontSize=16.sp
     - titleLarge: fontSize=22.sp, fontWeight=Bold
     - labelSmall: fontSize=16.sp (NEVER go below 16sp — accessibility rule)

  3. ui/theme/Theme.kt — wrap it all in a Material3 theme:
     - Create RushdulIlmTheme() Composable that wraps content
     - Use MaterialTheme with our custom colors and typography

  4. Modify MainActivity.kt to wrap RushdulIlmNavGraph() inside RushdulIlmTheme {}

DONE CONDITION:
  App compiles and runs.
  Background color is visible as the warm cream color.
  No red squiggly lines in theme files.

NEXT MICRO-TASK: P1.S3.SS1.MT2
```

---

#### Sub-sprint 1.3.2 — Build Home Screen Components

##### Micro-task P1.S3.SS1.MT2 — Create MicButton Composable Component
```
TASK:   Build the large microphone button as a reusable Composable component

WHY A SEPARATE COMPONENT FILE:
  The mic button will be the most important element of the app.
  Building it as a separate, reusable Composable (not inside HomeScreen.kt directly)
  means we can reuse it on other screens if needed, and test it independently.

FILE TO CREATE:
  Path: com/rushdululilm/app/ui/components/MicButton.kt

REQUIREMENTS:
  - @Composable fun MicButton(isRecording: Boolean, onClick: () -> Unit)
  - Size: fillMaxWidth, height = 0.4f of screen height (40% of screen height — rule)
  - Shape: Large circle (CircleShape)
  - Color when NOT recording: IslamicGreen (#2E7D32)
  - Color when RECORDING: ErrorRed (#B71C1C) — shows user mic is active
  - Icon: Mic icon (Icons.Default.Mic) — white, large size (72.dp)
  - Label below button (in same column): Telugu text "మైక్ నొక్కండి" + English "Press Mic"
  - Font: 18sp minimum — both languages shown, Telugu on top
  - Animation: add a pulsing scale animation when isRecording = true
    (use animateFloatAsState with infiniteRepeatable — explain what this does)
  - onClick: called when the button is tapped
  - Every parameter explained in a KDoc comment block above the function

DONE CONDITION:
  MicButton compiles without errors.
  You can preview it in Android Studio's Compose Preview (@Preview annotation).
  Green when isRecording=false, red when isRecording=true.

NEXT MICRO-TASK: P1.S3.SS1.MT3
```

##### Micro-task P1.S3.SS1.MT3 — Create LanguageSelector Composable
```
TASK:   Build a dropdown selector for choosing input/output language

FILE TO CREATE:
  Path: com/rushdululilm/app/ui/components/LanguageSelector.kt

REQUIREMENTS:
  - @Composable fun LanguageSelector(selectedLanguage: String, onLanguageSelected: (String) -> Unit)
  - Supported languages list:
    * "Telugu" (తెలుగు)
    * "Urdu" (اردو)
    * "Hindi" (हिंदी)
    * "English" (English)
  - Show as a Row: language icon + current selection text + dropdown arrow icon
  - On click: shows ExposedDropdownMenuBox with all 4 options
  - Each option shows: language name in that language + language name in English
    Example: "తెలుగు (Telugu)"
  - Font: minimum 18sp — labels big enough for illiterate users
  - Touch target: minimum 48dp height
  - Add a comment explaining what ExposedDropdownMenuBox is and why we use it

DONE CONDITION:
  LanguageSelector compiles and shows in Compose Preview.
  Clicking it opens a dropdown with all 4 languages.
  Selecting a language calls onLanguageSelected with the correct value.

NEXT MICRO-TASK: P1.S3.SS1.MT4
```

##### Micro-task P1.S3.SS1.MT4 — Create SourceSelector Composable
```
TASK:   Build a selector for choosing which Islamic knowledge source to query

FILE TO CREATE:
  Path: com/rushdululilm/app/ui/components/SourceSelector.kt

REQUIREMENTS:
  - @Composable fun SourceSelector(selectedSource: String, onSourceSelected: (String) -> Unit)
  - Sources list:
    * "islamqa_info" → Display name: "IslamQA.info (Neutral)"
    * "islamqa_org" → Display name: "IslamQA.org (Neutral)"
    * "deoband" → Display name: "Darul Ifta Deoband (Hanafi)"
    * "all" → Display name: "All Sources"
  - Show as a horizontal scrollable row of Chip/FilterChip composables
  - Selected chip: IslamicGreen background, white text
  - Unselected chip: grey outline, dark text
  - Each chip label: source display name, font 16sp minimum
  - Add a title above: "Source: / మూలం:" (English + Telugu)
  - Add a comment explaining what FilterChip is

DONE CONDITION:
  SourceSelector shows in Compose Preview.
  Tapping each chip selects it (green) and deselects others.
  onSourceSelected is called with the correct source key.

NEXT MICRO-TASK: P1.S3.SS1.MT5
```

##### Micro-task P1.S3.SS1.MT5 — Create HomeViewModel
```
TASK:   Create the ViewModel for HomeScreen using MVVM pattern

WHY MVVM (explain before code):
  MVVM stands for Model-View-ViewModel.
  - Model: the data (Islamic answers, language choice)
  - View: the UI (Composables you can see)
  - ViewModel: the brain between them — holds state, handles logic
  Think of it like a restaurant:
  - The kitchen (Model) has the food (data)
  - The dining room (View) is what customers see
  - The waiter (ViewModel) carries orders and food between them
  The ViewModel survives screen rotations — Composables do not.
  This means if you rotate your phone, you don't lose the answer being displayed.

FILE TO CREATE:
  Path: com/rushdululilm/app/viewmodel/HomeViewModel.kt

REQUIREMENTS:
  - @HiltViewModel annotation on the class
  - class HomeViewModel @Inject constructor() : ViewModel()
  - StateFlow properties (with detailed comments):
    * selectedLanguage: MutableStateFlow<String>("Telugu")
    * selectedSource: MutableStateFlow<String>("all")
    * isRecording: MutableStateFlow<Boolean>(false)
    * uiState: MutableStateFlow<HomeUiState>(HomeUiState.Idle)
  - Create a sealed class HomeUiState with states:
    * Idle — waiting for user to press mic
    * Recording — currently capturing audio
    * Processing — sending audio to server / on-device model
    * NavigatingToAnswer(answerId: String) — answer ready, navigate to AnswerScreen
    * Error(message: String) — something went wrong
  - Functions (placeholder — no real logic yet):
    * fun onMicPressed() — toggles isRecording, logs to console
    * fun onLanguageSelected(language: String)
    * fun onSourceSelected(source: String)
  - Explain StateFlow in comments: "StateFlow is like a live variable —
    when it changes, every Composable observing it automatically redraws"

DONE CONDITION:
  HomeViewModel.kt compiles without errors.
  @HiltViewModel annotation is present.
  All StateFlow properties are defined with comments.
  HomeUiState sealed class has all 5 states.

NEXT MICRO-TASK: P1.S3.SS1.MT6
```

##### Micro-task P1.S3.SS1.MT6 — Build the Full HomeScreen Layout
```
TASK:   Replace the placeholder HomeScreen with the real UI using components built above

FILE TO MODIFY:
  Path: com/rushdululilm/app/ui/screens/HomeScreen.kt

LAYOUT STRUCTURE (build this exactly):
  Scaffold (Material3 Scaffold gives us the app bar and structure)
  └── Column (vertical layout, fills entire screen)
      ├── TopAppBar — App name: "Rushd-ul-Ilm" + offline status indicator
      ├── OfflineBanner (shown only when offline) — orange banner at top
      ├── Spacer (16dp)
      ├── LanguageSelector (connected to HomeViewModel.selectedLanguage)
      ├── Spacer (12dp)
      ├── SourceSelector (connected to HomeViewModel.selectedSource)
      ├── Spacer (24dp)
      ├── MicButton — fills remaining 40% of screen
      │   onClick → homeViewModel.onMicPressed()
      │   isRecording → homeViewModel.isRecording
      ├── Spacer (16dp)
      └── Text — instruction text in Telugu + English:
               "మైక్ నొక్కి మీ ప్రశ్న అడగండి"
               "Press mic and ask your question"
               Font: 16sp, color = TextPrimary, textAlign = Center

REQUIREMENTS:
  - Connect to HomeViewModel via: val homeViewModel: HomeViewModel = hiltViewModel()
  - Observe state with: val isRecording by homeViewModel.isRecording.collectAsState()
  - Offline banner: orange (#E65100), text "📵 Offline Mode — Using Downloaded Knowledge"
  - Offline banner uses a fake isOffline = false for now (real detection in Phase 4)
  - Every Composable call has a comment explaining what it does

DONE CONDITION:
  HomeScreen shows all components when running on emulator.
  Tapping mic button changes its color from green to red (and back).
  Language and source selectors are functional.
  App does not crash.

SPRINT 1.3 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.4 — Answer Screen
NEXT MICRO-TASK: P1.S4.SS1.MT1
```

---

### SPRINT 1.4 — Answer Screen
**Goal:** A screen that shows a fake Islamic answer with source URL, a "Read Aloud" button, and 2 related video suggestions. All fake data — no real backend yet.

---

#### Sub-sprint 1.4.1 — Answer Screen Data Model & ViewModel

##### Micro-task P1.S4.SS1.MT1 — Create Answer Data Classes
```
TASK:   Create data classes that represent an Islamic answer and related video

WHY DATA CLASSES:
  A data class in Kotlin is like a blueprint for a piece of data.
  It automatically generates equals(), hashCode(), toString() — saving boilerplate.
  Think of it like a form template: every answer always has the same fields.

FILE TO CREATE:
  Path: com/rushdululilm/app/model/AnswerModels.kt

CLASSES TO CREATE (with full comments):
  1. data class FatwaAnswer(
         val id: String,          // Unique identifier for this answer
         val questionText: String, // The original question (in user's language)
         val answerText: String,  // The answer (translated to user's language)
         val sourceUrl: String,   // MANDATORY: URL of the source website
         val sourceName: String,  // Display name: "IslamQA.info" or "Darul Ifta Deoband"
         val language: String,    // Language of this answer: "Telugu", "Urdu", etc.
         val isOfflineCache: Boolean = false // True if from local SQLite cache
     )

  2. data class RelatedVideo(
         val id: String,          // Unique video file identifier
         val title: String,       // Video title (from filename or metadata)
         val scholarName: String, // Name of the scholar giving the lecture
         val durationSeconds: Int,// Video duration in seconds
         val filePath: String,    // Local file path (for offline play)
         val serverUrl: String    // Server URL (for online streaming)
     )

  3. Create a fake/placeholder FatwaAnswer in a companion object:
     FatwaAnswer.PLACEHOLDER — filled with realistic-looking sample data in Telugu

DONE CONDITION:
  AnswerModels.kt compiles without errors.
  PLACEHOLDER instance has realistic Telugu sample text.

NEXT MICRO-TASK: P1.S4.SS1.MT2
```

##### Micro-task P1.S4.SS1.MT2 — Create AnswerViewModel
```
TASK:   Create the ViewModel for AnswerScreen

FILE TO CREATE:
  Path: com/rushdululilm/app/viewmodel/AnswerViewModel.kt

REQUIREMENTS:
  - @HiltViewModel class AnswerViewModel @Inject constructor() : ViewModel()
  - StateFlow<FatwaAnswer?> currentAnswer — starts with FatwaAnswer.PLACEHOLDER (fake)
  - StateFlow<List<RelatedVideo>> relatedVideos — starts with 2 fake RelatedVideo objects
  - StateFlow<Boolean> isReadingAloud — true when TTS is speaking
  - StateFlow<Boolean> isLoading — true while fetching answer
  - Functions (placeholder):
    * fun loadAnswer(answerId: String) — for now: sets currentAnswer to PLACEHOLDER
    * fun onReadAloudPressed() — toggles isReadingAloud, prints "TTS placeholder"
    * fun onVideoClicked(video: RelatedVideo) — prints "Video clicked: ${video.title}"

DONE CONDITION:
  AnswerViewModel.kt compiles without errors.
  All StateFlow properties initialized with fake data.

NEXT MICRO-TASK: P1.S4.SS1.MT3
```

##### Micro-task P1.S4.SS1.MT3 — Build AnswerScreen Layout
```
TASK:   Build the complete AnswerScreen UI with all required components

FILE TO MODIFY:
  Path: com/rushdululilm/app/ui/screens/AnswerScreen.kt

LAYOUT (build this exactly, with comments on every line):
  Scaffold
  └── Column (scrollable — use LazyColumn for the main content)
      ├── TopAppBar — "Answer / సమాధానం" + Back button
      ├── SourceBadge — pill/chip showing source name (e.g., "IslamQA.info")
      │   Color: QuranicBlue background, white text
      ├── Spacer (16dp)
      ├── Text — answerText (large, minimum 18sp, color TextPrimary, selectable)
      ├── Spacer (24dp)
      ├── SourceUrlRow:
      │   Row {
      │     Text("Source: / మూలం:", 14sp, grey)
      │     ClickableText(sourceUrl, color=QuranicBlue, underlined)
      │     // Clicking opens the URL in the phone's browser
      │   }
      ├── Divider (thin horizontal line)
      ├── ReadAloudButton — ALWAYS VISIBLE (not hidden in a menu)
      │   Button — full width, 56dp height minimum
      │   Text: "🔊 Read Aloud / చదివి వినిపించు" (Telugu + English)
      │   Color: IslamicGreen when not reading, grey when already reading
      │   onClick → answerViewModel.onReadAloudPressed()
      ├── Divider
      ├── Text — "Related Lectures / సంబంధిత వీడియోలు" (section header, 18sp bold)
      ├── LazyRow — horizontal scrollable list of RelatedVideo cards
      │   Each card:
      │     Card (shadow, rounded corners)
      │     └── Column
      │         ├── Text(video.scholarName, 14sp, grey)
      │         ├── Text(video.title, 16sp, bold)
      │         └── Text(duration formatted as "12:34", 14sp)
      │         └── Button "▶ Play / ప్లే చేయి" → answerViewModel.onVideoClicked()

DONE CONDITION:
  AnswerScreen compiles and displays on emulator.
  Fake answer text is visible and large enough to read.
  Source URL is clickable (opens browser).
  Read Aloud button is visible and changes appearance when tapped.
  Related video cards are visible in horizontal scroll.

SPRINT 1.4 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.5 — Video Library Screen
NEXT MICRO-TASK: P1.S5.SS1.MT1
```

---

### SPRINT 1.5 — Video Library Screen
**Goal:** A searchable list of fake Islamic video lectures. Tapping a video shows its details. No actual video playback yet.

---

##### Micro-task P1.S5.SS1.MT1 — Create VideoLibraryViewModel + Fake Data
```
TASK:   Create ViewModel with a list of 5 fake Islamic video entries for testing

FILE TO CREATE: com/rushdululilm/app/viewmodel/VideoLibraryViewModel.kt

FAKE DATA (create these as hardcoded list for now):
  Create 5 RelatedVideo objects with realistic scholar names and titles like:
  - Scholar: Mufti Menk | Title: "Importance of Salah"
  - Scholar: Nouman Ali Khan | Title: "Understanding Surah Al-Baqarah"
  - Scholar: Dr. Zakir Naik | Title: "Science and Quran"
  - Scholar: Sheikh Omar Suleiman | Title: "The Names of Allah"
  - Scholar: Assim Al-Hakeem | Title: "Tafsir of Juz Amma"

REQUIREMENTS:
  - StateFlow<List<RelatedVideo>> allVideos — the full list of 5 fake videos
  - StateFlow<List<RelatedVideo>> filteredVideos — filtered by search query
  - StateFlow<String> searchQuery — the current search text
  - fun onSearchQueryChanged(query: String) — filters allVideos by title/scholar name

DONE CONDITION:
  VideoLibraryViewModel compiles.
  onSearchQueryChanged filters the list correctly (test with "Mufti").

NEXT MICRO-TASK: P1.S5.SS1.MT2
```

##### Micro-task P1.S5.SS1.MT2 — Build VideoLibraryScreen Layout
```
TASK:   Build the Video Library screen UI

FILE TO MODIFY: com/rushdululilm/app/ui/screens/VideoLibraryScreen.kt

LAYOUT:
  Scaffold
  └── Column
      ├── TopAppBar — "Video Library / వీడియో లైబ్రరీ"
      ├── SearchBar — TextField with:
      │   placeholder: "Search by topic or scholar... / అంశం లేదా పండితుడిని వెతకండి..."
      │   connected to: videoLibraryViewModel.searchQuery
      │   onValueChange → videoLibraryViewModel.onSearchQueryChanged()
      │   leadingIcon: search icon
      │   clearButton (X) when text is not empty
      └── LazyColumn — scrollable list of video cards
          Each card (VideoCard composable — create in ui/components/VideoCard.kt):
            Card (shadow, 8dp rounded corners, full width)
            └── Row
                ├── Box (thumbnail placeholder — grey rectangle, 80x60dp)
                │   Text: "▶" centered in the box
                └── Column
                    ├── Text(scholar, 14sp, IslamicGreen)
                    ├── Text(title, 16sp bold)
                    └── Text(duration, 14sp, grey)

DONE CONDITION:
  VideoLibraryScreen shows 5 fake video cards.
  Typing in the search bar filters the list in real time.
  Search works for both scholar name and video title.

SPRINT 1.5 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.6 — Settings Screen
NEXT MICRO-TASK: P1.S6.SS1.MT1
```

---

### SPRINT 1.6 — Settings Screen
**Goal:** A settings screen with toggles and buttons that look real but do nothing yet (UI only).

---

##### Micro-task P1.S6.SS1.MT1 — Build SettingsScreen Layout
```
TASK:   Build the Settings screen UI (all interactive elements UI-only, no real logic yet)

FILE TO MODIFY: com/rushdululilm/app/ui/screens/SettingsScreen.kt

SECTIONS TO BUILD:

  Section 1: Language Settings
    - Title: "Language / భాష"
    - RadioButton group: Telugu / Urdu / Hindi / English
    - Currently selected language highlighted in IslamicGreen

  Section 2: Madhab (Islamic school) Preference
    - Title: "Islamic Source Preference / ఇస్లామిక్ మూలం"
    - RadioButton group: All Sources / Neutral Only / Hanafi (Deoband)

  Section 3: Offline Knowledge Database
    - Title: "Download Offline Knowledge / ఆఫ్‌లైన్ జ్ఞానాన్ని డౌన్‌లోడ్ చేయండి"
    - For each source (islamqa.info, deoband):
      * Source name + "~500MB" / "~200MB" storage estimate
      * Download button (green) — shows "Download" or "Update Available"
      * Status text: "Not downloaded" / "Downloaded" / "Downloading..."
      * ProgressBar (shown during download — fake, stuck at 0% for now)

  Section 4: App Settings
    - Title: "App Settings"
    - Switch: "Auto-play read aloud" — Toggle (fake — does nothing yet)
    - Switch: "Large text mode" — Toggle (fake)
    - Text: "App Version: 1.0.0-alpha"

DONE CONDITION:
  SettingsScreen compiles and shows all 4 sections on emulator.
  RadioButtons and Switches visually respond to taps.
  Download buttons show but do nothing.

SPRINT 1.6 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.7 — String Resources & Theming
NEXT MICRO-TASK: P1.S7.SS1.MT1
```

---

### SPRINT 1.7 — String Resources & Accessibility Audit

##### Micro-task P1.S7.SS1.MT1 — Move All UI Strings to strings.xml
```
TASK:   Extract all hardcoded UI text from Compose files into strings.xml

WHY:
  Hardcoded strings in Kotlin files are a serious problem:
  1. If you want to change "Press Mic" to "Tap Mic", you'd search 20 files
  2. Future localization (adding more languages) becomes impossible
  3. It violates Android best practices and will cause Play Store warnings
  strings.xml is a single source of truth for all user-facing text.

FILE TO MODIFY: app/src/main/res/values/strings.xml

ADD ALL STRINGS in English:
  - app_name, home_screen_label, answer_screen_label, video_library_label, settings_label
  - mic_button_label, mic_button_hint, language_selector_label, source_selector_label
  - source_islamqa_info, source_islamqa_org, source_deoband, source_all
  - read_aloud_button, source_label, related_lectures_label
  - offline_banner_text, search_placeholder, download_button_label
  - (all other visible strings from all 4 screens)

CREATE: app/src/main/res/values-te/strings.xml (Telugu translations)
  - Same string names, Telugu values

UPDATE all Compose files to use stringResource(R.string.xxx) instead of hardcoded strings.

DONE CONDITION:
  No hardcoded Telugu or English strings remain in Kotlin files.
  App looks identical after the change (same strings, now from resources).
  App compiles and runs without errors.

NEXT MICRO-TASK: P1.S7.SS1.MT2
```

##### Micro-task P1.S7.SS1.MT2 — Accessibility Audit
```
TASK:   Check every UI element against the accessibility rules in AGENT_RULES.md

CHECKLIST (verify each item on the running emulator):
  [ ] Every button has a minimum touch target of 48dp × 48dp
  [ ] Mic button is at least 40% of screen height
  [ ] All text is minimum 16sp
  [ ] All button labels show Telugu + English (not English only)
  [ ] "Read Aloud" button is visible without scrolling on AnswerScreen
  [ ] Offline banner uses orange (#E65100) — not red or yellow
  [ ] Error states would show human-readable text (not HTTP codes)
  [ ] Color contrast ratio is sufficient (dark text on light background)

HOW TO CHECK SIZES:
  In Android Studio: Run → Layout Inspector
  Click on any element to see its exact dp size

FIX any violations found.

DONE CONDITION:
  All checklist items are verified and passing.
  No accessibility violations remain.

SPRINT 1.7 IS COMPLETE WHEN THIS MICRO-TASK IS DONE.
NEXT SPRINT: 1.8 — Phase 1 Integration Test
NEXT MICRO-TASK: P1.S8.SS1.MT1
```

---

### SPRINT 1.8 — Phase 1 Integration Test
**Goal:** The complete Phase 1 app is tested end-to-end. All screens work. No crashes. Documentation updated.

---

##### Micro-task P1.S8.SS1.MT1 — Full Smoke Test on Emulator
```
TASK:   Run through the complete app flow on the emulator and verify everything works

TEST SCRIPT (execute each step, note any failures):
  1. Launch app → HomeScreen appears with mic button, language selector, source selector
  2. Tap LanguageSelector → dropdown opens with 4 languages → select Urdu → selector updates
  3. Tap SourceSelector chips → each chip highlights green when selected
  4. Tap Mic button → button turns red
  5. Tap Mic button again → button turns green
  6. Navigate to Video Library via bottom nav → search bar appears, 5 fake videos shown
  7. Type "Mufti" in search → only Mufti Menk video remains visible
  8. Clear search → all 5 videos return
  9. Navigate to Settings via bottom nav → all 4 sections visible
  10. Tap a RadioButton in Language section → it selects visually
  11. Back button from Settings → returns to previous screen
  12. Force-rotate the emulator (Ctrl+F11) → app survives rotation, no crash

DOCUMENT any failures here. Fix before marking sprint complete.

DONE CONDITION:
  All 12 test steps pass with no crashes.

NEXT MICRO-TASK: P1.S8.SS1.MT2
```

##### Micro-task P1.S8.SS1.MT2 — Update All Documentation
```
TASK:   Update all documentation files to reflect Phase 1 completion

FILES TO UPDATE:
  1. activity-logs/ACTIVITY_LOG.md:
     - Update CURRENT_PHASE and PHASE_STATUS
     - Mark Phase 1 as COMPLETE in the phases overview
     - Add detailed session entry
     - Set NEXT_TASK to Phase 2, Sprint 2.1, Micro-task P2.S1.SS1.MT1

  2. Report Documentation/02_ANDROID_APP_LAYER.md:
     - Document every Kotlin file created in Phase 1
     - Include all code with full comments
     - Include folder structure diagram
     - Include screenshots descriptions of each screen

  3. SPRINT_SYSTEM.md:
     - Mark all Phase 1 sprints as COMPLETE in the tracker dashboard
     - Set CURRENT_SPRINT to 2.1

  4. If Graphiti running: add episode summarizing Phase 1 completion
  5. If Mem0 running: save "Phase 1 Android UI Skeleton COMPLETE. X files created."

DONE CONDITION:
  All documentation files updated.
  ACTIVITY_LOG.md shows Phase 1 as complete with NEXT_TASK pointing to Phase 2.

PHASE 1 IS COMPLETE.
NEXT: PHASE 2 — Backend Docker Services (Sprint 2.1 starts here)
```

---

## ══════════════════════════════════════════════════════
## PHASE 2 — BACKEND DOCKER SERVICES
## Goal: All local AI services (Ollama, Qdrant, FastAPI) running
##       in Docker containers with GPU acceleration enabled.
## ══════════════════════════════════════════════════════

---

### SPRINT 2.1 — Install Docker & NVIDIA Container Toolkit
**Goal:** Docker Engine installed, and NVIDIA GPU access verified from within a container.

---

#### Sub-sprint 2.1.1 — Install Docker Engine on Parrot OS

##### Micro-task P2.S1.SS1.MT1 — Install Docker Engine
```
TASK:   Install Docker Engine and Docker Compose on Parrot OS Linux
METHOD: Use the official Docker convenience script or apt repository.

STEP BY STEP:
  1. Open terminal in Parrot OS
  2. Uninstall old versions: sudo apt remove docker docker-engine docker.io containerd runc
  3. Install dependencies: sudo apt update && sudo apt install ca-certificates curl gnupg lsb-release
  4. Add Docker GPG key: 
     sudo mkdir -p /etc/apt/keyrings
     curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  5. Set up repository:
     echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  6. Install Docker: sudo apt update && sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin
  7. Start and enable Docker: sudo systemctl enable docker --now
  8. Add user to docker group: sudo usermod -aG docker $USER (log out and back in to apply)

DONE CONDITION:
  'docker --version' shows a valid version.
  'docker compose version' shows a valid version.
  'sudo docker run hello-world' runs successfully.

NEXT MICRO-TASK: P2.S1.SS1.MT2
```

##### Micro-task P2.S1.SS1.MT2 — Install NVIDIA Container Toolkit
```
TASK:   Install NVIDIA Container Toolkit so Docker can use the GTX 1650ti GPU

WHY:    Our AI models (Ollama, STT, TTS) need the GPU to run fast.
        Docker containers are isolated from the host hardware by default.
        The NVIDIA Container Toolkit is the "bridge" that lets a container
        talk to your NVIDIA GPU.

STEP BY STEP:
  1. Add the package repository:
     curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
     && curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
     sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
     sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list
  2. Install the toolkit: sudo apt update && sudo apt install nvidia-container-toolkit
  3. Configure Docker to use the toolkit: sudo nvidia-ctk runtime configure --runtime=docker
  4. Restart Docker: sudo systemctl restart docker
  5. Verify GPU access: docker run --rm --runtime=nvidia --gpus all nvidia/cuda:11.8.0-base-ubuntu22.04 nvidia-smi

DONE CONDITION:
  'nvidia-smi' output is visible from within the test Docker container.
  The output correctly identifies the GTX 1650ti with 4GB VRAM.

NEXT MICRO-TASK: P2.S2.SS1.MT1
```

---

### SPRINT 2.2 — Create docker-compose.yml Skeleton

##### Micro-task P2.S2.SS1.MT1 — Create Base docker-compose.yml
```
TASK:   Create the project's main Docker orchestration file

WHY:    Instead of starting 5 different programs manually, Docker Compose
        lets us start EVERYTHING with one command: 'docker compose up -d'.
        It handles networking between services and persistent storage.

FILE TO CREATE: backend/docker-compose.yml

REQUIREMENTS:
  - Define a custom network: 'rushd-network'
  - Define volumes for data persistence: 'ollama_data', 'qdrant_data'
  - Include service placeholders (comments only for now)
  - Every line commented for a beginner

DONE CONDITION:
  'docker compose config' shows no syntax errors.
  The backend/ folder exists.

NEXT MICRO-TASK: P2.S3.SS1.MT1
```

---

### SPRINT 2.3 — Start Ollama + pull Qwen3:4b model

##### Micro-task P2.S3.SS1.MT1 — Configure Ollama Service
```
TASK:   Add Ollama to docker-compose.yml and pull the local LLM model

STEP BY STEP:
  1. Add 'ollama' service to backend/docker-compose.yml
  2. Map port 11434:11434
  3. Configure GPU access using 'deploy.resources.reservations.devices'
  4. Run 'docker compose up -d ollama'
  5. Pull the model: docker exec -it ollama ollama run qwen2.5:3b (wait for download)
     Note: qwen3:4b might not be out yet, we use verified qwen2.5:3b/7b or similar that fits in 4GB.

DONE CONDITION:
  Ollama is running (check http://localhost:11434).
  Model is pulled and responsive to a test prompt.

NEXT MICRO-TASK: P2.S4.SS1.MT1
```

---

### SPRINT 2.4 — Build FastAPI Server Skeleton

##### Micro-task P2.S4.SS1.MT1 — Create FastAPI App & Requirements
```
TASK:   Create the main Python API server and its dependency list

FILE TO CREATE: 
  1. backend/fastapi_server.py
  2. backend/requirements.txt

REQUIREMENTS:
  - fastapi_server.py: basic app with @app.get("/health") endpoint
  - requirements.txt: fastapi, uvicorn, requests, pydantic
  - Full comments on every line

DONE CONDITION:
  Files exist in backend/ folder.
  No Python syntax errors.

NEXT MICRO-TASK: P2.S4.SS1.MT2
```

##### Micro-task P2.S4.SS1.MT2 — Containerize FastAPI Service
```
TASK:   Add the FastAPI service to docker-compose.yml

STEP BY STEP:
  1. Create backend/Dockerfile (Python 3.11-slim base)
  2. Add 'fastapi' service to docker-compose.yml
  3. Map port 8000:8000
  4. Run 'docker compose up -d --build'

DONE CONDITION:
  'curl http://localhost:8000/health' returns {"status": "healthy"}.

NEXT MICRO-TASK: P2.S5.SS1.MT1
```

---

### SPRINT 2.5 — Add Qdrant Vector DB Setup

##### Micro-task P2.S5.SS1.MT1 — Configure Qdrant Service
```
TASK:   Add Qdrant vector database to docker-compose.yml

STEP BY STEP:
  1. Add 'qdrant' service to backend/docker-compose.yml
  2. Map port 6333:6333
  3. Map volume for storage
  4. Run 'docker compose up -d qdrant'

DONE CONDITION:
  Qdrant dashboard is accessible at http://localhost:6333/dashboard.

NEXT MICRO-TASK: P2.S6.SS1.MT1
```

---

### SPRINT 2.6 — Wire FastAPI to Ollama

##### Micro-task P2.S6.SS1.MT1 — Implement /query Endpoint
```
TASK:   Add a test endpoint that takes a question and returns an AI answer

STEP BY STEP:
  1. Modify fastapi_server.py
  2. Add @app.post("/query") endpoint
  3. Use 'requests' to call Ollama API at 'http://ollama:11434/api/generate'
  4. Return the response to the user

DONE CONDITION:
  Postman or curl test to /query returns a real response from the LLM.

NEXT MICRO-TASK: P2.S7.SS1.MT1
```

---

### SPRINT 2.7 — Phase 2 Integration Test

##### Micro-task P2.S7.SS1.MT1 — Full Stack Smoke Test
```
TASK:   Verify all 3 services are talking to each other correctly

CHECKLIST:
  [ ] Docker starts all 3 services with one command
  [ ] GPU is active (check nvidia-smi while querying)
  [ ] VRAM usage is under 3.5GB
  [ ] FastAPI can reach Ollama

DONE CONDITION:
  All services green in 'docker compose ps'.

NEXT MICRO-TASK: P2.S7.SS1.MT2
```

##### Micro-task P2.S7.SS1.MT2 — Update Documentation
```
TASK:   Update Report Documentation/09_BACKEND_DOCKER.md

DONE CONDITION:
  Phase 2 marked complete in all logs.

*(Phase 2 micro-tasks will be written in full before Phase 2 begins.
 AI Agent: when Phase 1 is done, ask developer to confirm before starting Phase 2.
 Then add the full Phase 2 sprint breakdown here following the same format.)*

---

## ══════════════════════════════════════════════════════
## PHASES 3–6 SPRINT OUTLINES (detailed before each phase begins)
##
## Phase 3 — Knowledge Ingestion (Sprints 3.1–3.6)
##   3.1 Read existing scraper scripts + test them
##   3.2 Run fast_mirror.py for islamqa.info
##   3.3 Embed islamqa.info into Qdrant
##   3.4 Build LlamaIndex RAG pipeline
##   3.5 Test RAG with source-cited answers
##   3.6 Ingest Deoband data + test source filter
##
## Phase 4 — Connect Android to Backend (Sprints 4.1–4.5)
##   4.1 Create Retrofit API service interface
##   4.2 Wire mic button → /transcribe endpoint
##   4.3 Display real RAG answer on AnswerScreen
##   4.4 Implement network tier detection
##   4.5 Integration test + documentation
##
## Phase 5 — Multilingual + Offline (Sprints 5.1–5.7)
##   5.1 IndicTrans2 Docker service
##   5.2 Coqui XTTS-v2 Docker service + TTS in Telugu
##   5.3 whisper.cpp JNI (Android NDK + CMake)
##   5.4 Opus-MT ONNX offline translation
##   5.5 Android TTS API + voice pack download
##   5.6 Full offline mode integration test
##   5.7 Documentation
##
## Phase 6 — Video Library + Deployment (Sprints 6.1–6.6)
##   6.1 Python video metadata indexing script
##   6.2 Whisper batch transcription of video folder
##   6.3 Embed video transcripts into Qdrant
##   6.4 ExoPlayer integration (local + server streaming)
##   6.5 SQLite offline DB export + Android download
##   6.6 Nginx + HTTPS on VPS + final deployment
## ══════════════════════════════════════════════════════

---

## 📏 MICRO-TASK RULES FOR AI AGENTS

```
RULE MT1: Do ONE micro-task per session. Never combine two micro-tasks into one session.
          If a micro-task is too small (finished in 5 minutes), do the NEXT one too.
          If a micro-task is too large (still not done after 40 minutes), STOP,
          document what was done, and split it by adding MT3b, MT3c etc.

RULE MT2: Every micro-task must end with a verified DONE CONDITION.
          Never mark a micro-task complete without checking its DONE CONDITION.

RULE MT3: Never start the next micro-task if the current one's DONE CONDITION fails.
          Fix the current task first. Document the blocker in ACTIVITY_LOG.md.

RULE MT4: At the end of every micro-task, update ACTIVITY_LOG.md with:
          CURRENT_MICRO_TASK: [completed task ID]
          NEXT_MICRO_TASK: [next task ID]
          This is what the next AI agent session reads to know where to start.

RULE MT5: Never skip a micro-task "because it seems obvious."
          Every micro-task exists for a reason. If it seems too easy, do it quickly.

RULE MT6: When a micro-task produces a file, that file MUST have:
          - Header comment block (see AGENT_RULES.md CODE QUALITY RULES)
          - Line-by-line comments on every line of code
          - A KDoc comment above every function
```

---

*Rushd-ul-Ilm (رشد العلم) — Sprint System v1.0 | Shaik Hidayatullah, Kurnool, India*
ia*
