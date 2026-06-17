# 02_ANDROID_APP_LAYER.md
# Rushd-ul-Ilm (رشد العلم) — Android App Layer Documentation
# Last Updated: 2026-05-30 | Updated by: Codex
# Status: IN PROGRESS — Phase 1 Android setup started
# Changes: Documented Android Studio installation, KVM verification, and Pixel_5_API_31 emulator setup

---

## Purpose Of This File

This report explains the Android layer of Rushd-ul-Ilm in beginner-friendly language.
It will grow during Phase 1 as the Android Studio project, Kotlin files, Jetpack Compose screens, ViewModels, and resources are created.

---

## Micro-task P1.S1.SS1.MT1 — Install Android Studio

### What Was Verified

Android Studio is installed on the developer machine and launches from this project-approved binary:

```text
/media/hidayat/PersonalData/Kali_Linux_Files/android-studio/panda4/bin/studio
```

The installed version reported by the terminal is:

```text
Android Studio Panda 4 | 2025.3.4 Patch 1
Build #AI-253.32098.37.2534.15336583
```

The Android SDK folder already contains Android platform versions higher than the project minimum API 26:

```text
android-28
android-33
android-34
android-35
android-36.1
```

### Beginner Explanation

Android Studio is the main program used to build Android apps.
It is like a full workshop: the editor is where we write Kotlin code, Gradle builds the app, the SDK provides Android tools, and the emulator runs a fake phone for testing.

The Android SDK is like a toolbox containing different Android versions.
Because Rushd-ul-Ilm must support Android 8.0 and higher, we need API 26 or newer installed; this machine has API 28 and newer, so that requirement is met.

### Result

Micro-task `P1.S1.SS1.MT1` is complete.
The next micro-task is `P1.S1.SS1.MT2 — Enable KVM for Android Emulator`.

---

## Micro-task P1.S1.SS1.MT2 — Enable KVM For Android Emulator

### What Was Verified

The CPU supports Intel virtualization, shown by the `vmx` flag in `/proc/cpuinfo`.

The Linux kernel has the KVM modules loaded:

```text
kvm_intel
kvm
```

The host system has the KVM device available:

```text
crw-rw----+ 1 root kvm 10, 232 May 29 16:16 /dev/kvm
```

The `kvm-ok` command reports:

```text
INFO: /dev/kvm exists
KVM acceleration can be used
```

The user `hidayat` has read/write access through an ACL entry:

```text
user:hidayat:rw-
```

### Beginner Explanation

The Android emulator is a virtual phone.
Without KVM, Linux has to simulate the phone CPU slowly in software.
With KVM, the emulator can use the laptop CPU virtualization feature directly, similar to how VirtualBox or VMware runs virtual machines faster when hardware acceleration is enabled.

The `/dev/kvm` file is the doorway that emulator software uses to talk to KVM.
For the emulator to run fast, that doorway must exist and the developer user must have permission to open it.

### Result

Micro-task `P1.S1.SS1.MT2` is complete.
The next micro-task is `P1.S1.SS1.MT3 — Create Android Virtual Device (Emulator)`.

---

## Micro-task P1.S1.SS1.MT3 — Create Android Virtual Device

### What Was Created

The requested Android Virtual Device was created:

```text
Pixel_5_API_31
```

The AVD uses this device profile and Android version:

```text
Device profile: pixel_5
Target: android-31
System image: system-images/android-31/google_apis/x86_64
```

### Supporting SDK Packages Installed

The Android SDK command-line tools were added here:

```text
/media/hidayat/PersonalData/Kali_Linux_Files/AndroidStudioPanda2Additional/Android/Sdk/cmdline-tools/latest
```

Android 12 API 31 was installed here:

```text
/media/hidayat/PersonalData/Kali_Linux_Files/AndroidStudioPanda2Additional/Android/Sdk/platforms/android-31
```

The Android 12 emulator system image was installed here:

```text
/media/hidayat/PersonalData/Kali_Linux_Files/AndroidStudioPanda2Additional/Android/Sdk/system-images/android-31/google_apis/x86_64
```

The AVD files were created here:

```text
/media/hidayat/PersonalData/Kali_Linux_Files/AndroidStudioPanda2Additional/.android/avd/Pixel_5_API_31.avd
```

### Verification

The emulator list now includes:

```text
Pixel_5_API_31
Pixel_6
Pixel_7
```

The emulator booted successfully and Android reported:

```text
boot_completed=1
```

The active Android launcher was:

```text
com.google.android.apps.nexuslauncher/.NexusLauncherActivity
```

ADB input commands were sent successfully, confirming the virtual phone can be interacted with.

### Beginner Explanation

An Android Virtual Device, or AVD, is a fake Android phone stored on your computer.
It is like a saved VirtualBox machine, but instead of running Windows or Linux, it runs Android.

The device profile controls the phone shape, screen size, RAM, cameras, and hardware settings.
The system image controls the Android version installed inside that fake phone.
For this project, `Pixel_5_API_31` means a Pixel 5-style virtual phone running Android 12.

### Result

Micro-task `P1.S1.SS1.MT3` is complete.
The next micro-task is `P1.S1.SS2.MT1 — Create New Android Project`.

### Mem0 Session Memory Repair

Before starting `P1.S1.SS2.MT1`, the local project memory helper was repaired and verified.
The old helper was using a Gemini cloud configuration and a hardcoded API key, which conflicts with the project's local-only AI rule.

The helper at `knowledge-graph/mem0_helper.py` now:

- uses local Ollama models instead of a cloud provider
- disables Mem0 telemetry so no PostHog analytics are sent
- saves session checkpoints with `infer=False` so memory entries are stored directly and quickly
- stores memory files in stable local paths under `knowledge-graph/`

Three local memories were saved successfully:

- the current project state and current micro-task
- the beginner-teaching preference
- the required Android Studio binary path

Verification was done by running a Mem0 search for the current phase and micro-task, and the saved session state was returned correctly.

---

## Micro-task P1.S1.SS2.MT1 — Create New Android Project

### What Was Created

A complete Android project was initialized in the `android-app/` directory. This includes:

- **Gradle Build System:** `settings.gradle.kts`, `build.gradle.kts`, and `libs.versions.toml` (Version Catalog).
- **App Manifest:** `AndroidManifest.xml` which identifies the app as `com.rushdululilm.app`.
- **Main Activity:** `MainActivity.kt` set up with Jetpack Compose.
- **UI Theme:** Custom colors (`Color.kt`), typography (`Type.kt`), and a Material3 theme (`Theme.kt`).
- **Build Verification:** Successfully ran `./gradlew help` to confirm the project structure is valid.

### Beginner Explanation

Creating a new project is like setting up a new notebook for a class. We've labeled the cover (Package Name), set up the table of contents (Gradle), and wrote the first "Hello World" on the first page (MainActivity).

- **Gradle** is our project's manager. It downloads the code we need from the internet and compiles our code into an app.
- **Jetpack Compose** is how we draw the screen. Instead of using separate design files, we write Kotlin functions that "compose" the UI.
- **The Theme** is our app's style guide. It ensures that every screen uses the same colors and font sizes, making the app look professional and consistent.

### Result

Micro-task `P1.S1.SS2.MT1` is complete.
The next micro-task is `P1.S1.SS2.MT2 — Add Dependencies to build.gradle.kts`.

---

## Micro-task P1.S1.SS2.MT4 — Run the App for the First Time

### What Was Verified

The Rushd-ul-Ilm app was successfully built, installed, and launched on the `Pixel_5_API_31` emulator.

- **Build Success:** All dependencies and themes are correctly configured.
- **Launch Success:** The app starts and displays the main greeting screen.
- **Stability:** Logcat shows no crashes or errors during startup.
- **Active Screen:** Verified that `MainActivity` is the currently resumed activity on the device.

### Beginner Explanation

Running the app for the first time is like "Turning the Key" in a car you just built. It proves that all the parts we've added so far—the libraries, the code, and the settings—are working together perfectly. 

We used **ADB (Android Debug Bridge)** to push the app from our computer to the virtual phone. Once it was there, we told the phone to "Open the front door" (MainActivity). Seeing the "Hello Android!" message means our foundation is solid and we are ready to start building the actual features.

### Result

Sprint 1.1 is **COMPLETE**.
The foundation is laid. We have a working project, a working emulator, and a verified build process.
The next phase is **Sprint 1.2 — App Structure & Navigation**.

---

## Micro-task P1.S2.SS1.MT1 — Create Package Folder Structure

### What Was Created

The standard MVVM architecture folder structure was created inside `com.rushdululilm.app`:

- `ui/screens` — Jetpack Compose screens
- `ui/components` — Reusable UI components
- `viewmodel` — MVVM ViewModels
- `data/local` — Room DB entities and DAOs
- `data/remote` — Retrofit interfaces
- `data/repository` — Data repositories
- `di` — Hilt Dependency Injection modules
- `utils` — Helper functions
- `model` — Kotlin data classes

A placeholder Kotlin file with explanatory comments was added to each package so they appear in Android Studio.

### Beginner Explanation

Just like organizing a kitchen with separate drawers for knives, plates, and spices, we organize our code into "packages" (folders). This makes it easy to find things as the app grows. MVVM (Model-View-ViewModel) is the standard blueprint for Android apps: 
- `data` (Model) gets the ingredients.
- `ui` (View) presents the final dish.
- `viewmodel` acts as the chef, coordinating between them.

### Result

Micro-task `P1.S2.SS1.MT1` is complete.
The next micro-task is `P1.S2.SS1.MT2 — Create Route Constants File`.

---

## Micro-task P1.S2.SS1.MT2 — Create Route Constants File

### What Was Created

A central file for navigation route strings was created:
- `android-app/app/src/main/java/com/rushdululilm/app/ui/screens/Routes.kt`

This file contains constants for all planned screens: `HOME`, `ANSWER`, `VIDEO_LIBRARY`, and `SETTINGS`.

### Beginner Explanation

Think of Jetpack Compose Navigation like a city map. Every screen is a different house, and every house needs an address. Instead of typing "home" or "settings" manually every time we want to change screens, we store them in one file as constants (values that never change). This prevents spelling mistakes and makes the code cleaner.

### Result

Micro-task `P1.S2.SS1.MT2` is complete.
The next micro-task is `P1.S2.SS2.MT1 — Create 4 Empty Placeholder Screens`.

---

## Micro-task P1.S2.SS2.MT1 — Create Placeholder Screens

### What Was Created

Four basic Jetpack Compose screen files were created inside `com.rushdululilm.app.ui.screens`:
- `HomeScreen.kt`
- `AnswerScreen.kt`
- `VideoLibraryScreen.kt`
- `SettingsScreen.kt`

Each file contains a single `@Composable` function that displays its name as a text placeholder in the center of the screen.

### Beginner Explanation

In Android, `@Composable` is a special tag that tells the system, "This function draws a part of the screen." Think of it like a painter. When the app wants to show the Home Screen, it calls the `HomeScreen()` function, and that function paints a Box (a container) with some Text inside it. We have created four empty "canvases" ready to be filled with our actual app designs.

### Result

Micro-task `P1.S2.SS2.MT1` is complete.
The next micro-task is `P1.S2.SS2.MT2 — Create the Navigation Graph`.

---

## Micro-task P1.S2.SS2.MT2 — Create the Navigation Graph

### What Was Created

A central navigation controller was implemented in `NavGraph.kt`. This file acts as the app's traffic controller, managing the movement between different screens.

- **Navigation Controller:** Created using `rememberNavController()` to track the user's history and change screens.
- **NavHost:** A frame that swaps between `HomeScreen`, `AnswerScreen`, `VideoLibraryScreen`, and `SettingsScreen` based on the active route.
- **Bottom Navigation Bar:** A Material3 `NavigationBar` was added to the bottom of the screen with three icons:
  - **Home:** Navigates to the main mic screen.
  - **Videos:** Navigates to the searchable video library.
  - **Settings:** Navigates to app preferences and offline downloads.

### Beginner Explanation

Imagine the app is like a house with four rooms.
- **NavGraph** is the hallway connecting all the rooms.
- **NavController** is the remote control that lets you choose which room to walk into.
- **NavHost** is the "magic door" that changes its destination when you press a button on the remote.

We also added a **Bottom Bar** which is like a shortcut panel at the bottom of your phone. It allows you to jump between the Home, Video, and Settings "rooms" instantly from anywhere in the app.

### Result

Micro-task `P1.S2.SS2.MT2` and `MT3` are complete.
The app now supports full navigation between its core functional areas.
The next sprint is **Sprint 1.3 — Home Screen**.


---

## Micro-task P1.S3.SS1.MT1 — Create App Theme (Colors & Typography)

### What Was Created

A custom Material3 theme was established to maintain a cohesive visual identity across the app. This was broken down into three files:

- **`ui/theme/Color.kt`:** Defines the explicit color palette (e.g., IslamicGreen, QuranicBlue, BackgroundCream).
- **`ui/theme/Type.kt`:** Dictates typography rules. Notably, this enforces a strict accessibility baseline of 16sp for all text.
- **`ui/theme/Theme.kt`:** Assembles the colors and fonts into a MaterialTheme wrapper (\`RushdulIlmTheme\`) that is applied at the root of the UI hierarchy.

### Beginner Explanation

Just as a house needs a defined color palette before painting, an app needs a theme before placing buttons or text. By declaring colors and fonts in one central place, we guarantee that the app looks consistent. If we decide to change the shade of "Islamic Green," we only have to change it in `Color.kt`, and the whole app updates immediately.

### Result

Micro-task `P1.S3.SS1.MT1` is complete.
The app theme is configured.
The next micro-task is `P1.S3.SS1.MT2 — Create MicButton Composable Component`.


---

## Micro-task P1.S3.SS1.MT2 — Create MicButton Composable Component

### What Was Created

A reusable, interactive microphone button (\`MicButton.kt\`) was implemented to serve as the primary action element on the Home screen.

- **Component Design:** The button occupies approximately 40% of the screen height, adhering to the large touch-target requirements.
- **Dynamic Styling:** It utilizes the established \`RushdulIlmTheme\` colors, appearing \`IslamicGreen\` by default and switching to \`ErrorRed\` when the \`isRecording\` state is active.
- **Animation:** An infinite pulsing animation (\`rememberInfiniteTransition\`) is applied when recording to provide clear visual feedback to the user.
- **Accessibility:** It features bilingual labels (Telugu and English) positioned directly below the button, enforcing the minimum 16sp typography rule.
- **Dependencies:** Added \`material-icons-extended\` to access the required microphone icon.

### Beginner Explanation

The Mic button is the heart of the app. We built it as a "Reusable Component" rather than just drawing it directly on the Home screen. This is like building a pre-fabricated window instead of building a window frame directly into a wall; we can take this pre-built button and easily drop it into any screen we want in the future. The button also has a "state" (recording or not recording). When the state changes, the button automatically repaints itself in a different color and starts a pulsing animation, without us needing to manually redraw it.

### Result

Micro-task \`P1.S3.SS1.MT2\` is complete.
The MicButton component is ready.
The next micro-task is \`P1.S3.SS1.MT3 — Create LanguageSelector Composable\`.


---

## Micro-task P1.S3.SS1.MT3 — Create LanguageSelector Composable

### What Was Created

A dropdown component (\`LanguageSelector.kt\`) was implemented to handle language preferences.

- **Component Design:** Utilizes the recommended Material3 \`ExposedDropdownMenuBox\` pattern to provide a clean, modern dropdown experience.
- **Languages:** Supports Telugu, Urdu, Hindi, and English out of the box.
- **Accessibility:** Enforces a minimum 56dp touch target for the collapsed selector and 48dp for individual menu items. All text utilizes the 18sp minimum typography defined in the theme.
- **State Handling:** Takes a \`selectedLanguage\` state and an \`onLanguageSelected\` lambda to communicate user choices back to the parent ViewModel.

### Beginner Explanation

A "Dropdown Menu" in Android used to be complicated to build. Jetpack Compose gives us \`ExposedDropdownMenuBox\`, which handles opening, closing, and animating the menu for us. We pass it the currently selected language (so it knows what to show in the box) and a function called \`onLanguageSelected\`. When the user taps a different language in the list, the component calls this function to tell the "brain" (ViewModel) to update the app.

### Result

Micro-task \`P1.S3.SS1.MT3\` is complete.
The LanguageSelector component is ready.
The next micro-task is \`P1.S3.SS1.MT4 — Create SourceSelector Composable\`.


---

## Micro-task P1.S3.SS1.MT4 — Create SourceSelector Composable

### What Was Created

A \`SourceSelector.kt\` component was built to allow the user to select their desired Islamic knowledge database.

- **Component Design:** It uses a \`LazyRow\` to provide a horizontally scrolling list of options. The items are rendered as \`FilterChip\` composables.
- **Sources Supported:** The component maps the four predefined sources ("all", "islamqa_info", "islamqa_org", "deoband") to their readable display names.
- **Styling:** When a chip is selected, its background changes to \`IslamicGreen\` with white text, providing clear visual feedback. Unselected chips retain the default outlined appearance.
- **Typography:** The section title uses \`bodyLarge\` and the chip labels use \`labelSmall\` (enforcing the 16sp accessibility minimum).

### Beginner Explanation

When asking a question, users might only want fatwas from a specific school of thought or website. The "Source Selector" is a horizontal list of buttons (called "Chips" in Android). We use a \`LazyRow\` which means the list can be scrolled side-to-side, and Android only draws the chips that are currently visible on the screen, saving memory. When a user taps a chip, it turns green, and it tells the "brain" (ViewModel) which source to use for the search.

### Result

Micro-task \`P1.S3.SS1.MT4\` is complete.
The SourceSelector component is ready.
The next micro-task is \`P1.S3.SS1.MT5 — Create HomeViewModel\`.


---

## Micro-task P1.S3.SS1.MT5 — Create HomeViewModel

### What Was Created

The `HomeViewModel.kt` was created to serve as the MVVM "brain" for the Home screen, managing the UI state and coordinating user actions.

- **Architecture:** It utilizes Hilt (`@HiltViewModel`) for dependency injection, allowing the system to provide it automatically to the `HomeScreen`.
- **State Management:** Uses Coroutines `StateFlow` to manage:
  - `selectedLanguage`: Defaults to "Telugu".
  - `selectedSource`: Defaults to "all".
  - `isRecording`: Toggles between true/false.
  - `uiState`: Tracks the screen phase using a sealed class (`HomeUiState`) with states like `Idle`, `Recording`, `Processing`, `NavigatingToAnswer`, and `Error`.
- **Action Handlers:** Includes functions (`onMicPressed`, `onLanguageSelected`, `onSourceSelected`) that update these state flows. These act as placeholders for the real offline logic (like AudioRecord API access) that will be integrated in Phase 4.

### Beginner Explanation

The ViewModel is like the "waiter" in a restaurant. The UI (the buttons and menus we just built) is the dining room. When a customer (user) taps a button, the UI tells the waiter "I want to record." The waiter (ViewModel) writes this down on his notepad (`StateFlow`). Because the UI is always watching the waiter`s notepad, it instantly redraws the button in red as soon as the waiter writes "Recording = true". This separation means the button doesn`t have to know *how* to record audio; it only knows how to look when recording is happening.

### Result

Micro-task `P1.S3.SS1.MT5` is complete.
The HomeViewModel is ready.
The next micro-task is `P1.S3.SS1.MT6 — Build the Full HomeScreen Layout`.

---

## Micro-task P1.S3.SS1.MT6 — Build the Full HomeScreen Layout

### What Was Created

The full `HomeScreen.kt` layout was implemented, assembling all the individual components into a cohesive, functional screen.

- **Scaffold Structure:** Uses a `Scaffold` to provide a consistent `TopAppBar` across the screen.
- **Component Integration:** The `LanguageSelector`, `SourceSelector`, and `MicButton` were placed into a vertical `Column`.
- **ViewModel Binding:** The screen is bound to `HomeViewModel` using `hiltViewModel()`. State is observed via `collectAsState()` and passed down to the components.
- **Bilingual Instructions:** Added the required instruction text in Telugu and English at the bottom of the screen.
- **Offline Banner:** Implemented a placeholder `OfflineBanner` that utilizes the custom `OfflineOrange` color.
- **Accessibility:** Enforced 18sp+ typography for instructions and ensured all components respect the `Scaffold` padding values.

### Beginner Explanation

This task was like assembling a car in a factory. We had all the pre-built parts (the mic button, the language dropdown, the source chips), and now we have bolted them together onto the "frame" (the Scaffold). 

We also connected the "electronics" (the HomeViewModel). Now, when you pick a language from the menu, that choice travels through the wires to the "brain" (ViewModel), which remembers it. When you press the Mic button, the brain tells the button to turn red. Everything is now connected and working as one single screen!

### Result

Micro-task `P1.S3.SS1.MT6` is complete.
Sprint 1.3 is **COMPLETE** (pending final polish).
The next sprint is **Sprint 1.4 — Answer Screen**.

---

## Micro-task P1.S4.SS1.MT1 — Create Answer Data Classes

### What Was Created

A new file `AnswerModels.kt` was created in the `model` package. This file defines two essential data structures:

- **`FatwaAnswer`**: A data class representing a single Islamic answer. It enforces the presence of critical fields like the original question, translated answer, and crucially, the `sourceUrl` and `sourceName`. It also includes a `PLACEHOLDER` object containing realistic Telugu sample text for use during UI development.
- **`RelatedVideo`**: A data class representing an Islamic video lecture that relates to the fatwa answer, containing metadata like the title, scholar name, duration, and file paths.

### Beginner Explanation

A "data class" in Kotlin is like a blueprint or a blank form. Before we can display an answer on the screen, we need to know exactly what information makes up an "answer." The `FatwaAnswer` data class tells Android that every answer *must* have an ID, a question, the actual text, and a web link to the source. 

By creating a `PLACEHOLDER` object inside this class, we've filled out one of these forms with fake (but realistic-looking) Telugu text. This allows us to build and test the Answer Screen's design without needing to connect to the real database just yet.

### Result

Micro-task `P1.S4.SS1.MT1` is complete.
The data models for the Answer Screen are established.
The next micro-task is `P1.S4.SS1.MT2 — Create AnswerViewModel`.

---

## Micro-task P1.S4.SS1.MT2 — Create AnswerViewModel

### What Was Created

A new file `AnswerViewModel.kt` was created in the `viewmodel` package. This serves as the "brain" for the Answer Screen. 

- **State Management:** It uses Coroutines `StateFlow` to securely hold and manage the data. It tracks the `currentAnswer` (loading our fake Telugu placeholder), a list of `relatedVideos` (loading two fake video entries), and states like `isReadingAloud` and `isLoading`.
- **Dependency Injection:** It is annotated with `@HiltViewModel`, which means the Android system knows how to automatically create and provide this "brain" to our screen when it asks for it.
- **Action Handlers:** It includes placeholder functions for actions the user might take, such as pressing the "Read Aloud" button or clicking on a video.

### Beginner Explanation

If the Data Classes are the recipe, the ViewModel is the chef. When the user asks a question on the Home Screen and the app navigates to the Answer Screen, the Answer Screen needs to know what to display. It asks the `AnswerViewModel`.

The ViewModel holds the answer safely in a `StateFlow`. Think of a `StateFlow` like a live TV broadcast. The screen tunes into this broadcast. Whenever the ViewModel changes the channel (updates the answer), the screen automatically shows the new program without needing to be told twice. Right now, our ViewModel is just broadcasting "reruns" (the fake placeholder data) until we build the real backend in Phase 2.

### Result

Micro-task `P1.S4.SS1.MT2` is complete.
The AnswerViewModel is ready.
The next micro-task is `P1.S4.SS1.MT3 — Build AnswerScreen Layout`.

---

## Micro-task P1.S4.SS1.MT3 — Build AnswerScreen Layout

### What Was Created

The full `AnswerScreen.kt` layout was implemented. This screen is the destination after a user asks a question.

- **Layout Structure:** It utilizes a `Scaffold` with a `TopAppBar` (containing a back button) and a `LazyColumn` for the main content body.
- **Answer Display:** Shows a badge with the source name (e.g., "IslamQA.info") and the translated fatwa answer using the required 18sp minimum typography.
- **Source Link:** A `ClickableText` element was added that opens the phone's web browser to the original fatwa URL when tapped. This satisfies a strict project requirement.
- **Read Aloud:** A large, prominent button spanning the full width was added for the TTS feature. Its color dynamically changes based on the `isReadingAloud` state from the ViewModel.
- **Related Videos:** A `LazyRow` was implemented at the bottom of the screen to display a horizontally scrolling list of `VideoCard` composables, showing the fake related video data.

### Beginner Explanation

The Answer Screen is like a long scroll of paper. Because an Islamic answer can be very detailed, we can't just put it in a normal box on the screen; we have to make it scrollable. In Android, we use a `LazyColumn` for this. It's "lazy" because it only does the work of drawing the parts of the text that you are currently looking at.

We also added a "Read Aloud" button directly under the answer. This is crucial for users who cannot read well. Finally, at the very bottom, we added a "carousel" (a `LazyRow`) of related videos. This allows users to swipe left and right to see lectures related to their question without leaving the page.

### Result

Micro-task `P1.S4.SS1.MT3` is complete.
Sprint 1.4 is **COMPLETE**.
The next sprint is **Sprint 1.5 — Video Library Screen**.

---

## Micro-task P1.S5.SS1.MT1 — Create VideoLibraryViewModel + Fake Data

### What Was Created

A new file \`VideoLibraryViewModel.kt\` was created in the \`viewmodel\` package. This serves as the state manager for the Video Library Screen.

- **Fake Data:** It initializes a hardcoded list of 5 \`RelatedVideo\` entries with realistic titles and scholar names (e.g., Mufti Menk, Nouman Ali Khan).
- **State Management:** It uses \`StateFlow\` to track the complete list (\`allVideos\`), the user's current search input (\`searchQuery\`), and the list of videos matching the search (\`filteredVideos\`).
- **Search Filtering:** The \`onSearchQueryChanged\` function dynamically filters the video list by checking if the search text is contained within either the video title or the scholar's name, ignoring case sensitivity.

### Beginner Explanation

The Video Library Screen needs to display a list of videos and let the user search through them. The \`VideoLibraryViewModel\` acts like a helpful librarian.

When the screen first opens, the librarian holds the complete catalog of videos (\`allVideos\`) and places them all on the display shelf (\`filteredVideos\`). When the user starts typing a name into the search bar, the UI tells the librarian what was typed (\`searchQuery\`). The librarian quickly checks every video's title and scholar name, picks out the ones that match, and updates the display shelf so the user only sees the relevant videos. We've stocked the library with 5 fake videos for now so we can test the search feature.

### Result

Micro-task \`P1.S5.SS1.MT1\` is complete.
The VideoLibraryViewModel is ready.
The next micro-task is \`P1.S5.SS1.MT2 — Build VideoLibraryScreen Layout\`.

---

## Micro-task P1.S5.SS1.MT2 — Build VideoLibraryScreen Layout

### What Was Created

The `VideoLibraryScreen.kt` layout was fully implemented. This screen serves as a searchable catalog of Islamic video lectures.

- **Reusable Component:** Created `ui/components/VideoCard.kt`. This is a horizontal card displaying a placeholder thumbnail, scholar name, title, and formatted time. It's built to be reused anywhere a list of videos is needed.
- **Search Bar:** Added an `OutlinedTextField` that binds directly to the `searchQuery` in the `VideoLibraryViewModel`. It features a clear button (X) that only appears when text is present.
- **Dynamic List:** Implemented a `LazyColumn` that observes the `filteredVideos` from the ViewModel. As the user types in the search bar, this list instantly updates to show only matching videos. An empty state message is shown if no videos match the query.

### Beginner Explanation

The Video Library Screen is like a searchable filing cabinet. We built a search bar at the top and a scrolling list at the bottom. 

We also built a reusable "Video Card." Think of this like a standardized index card; it always has the picture on the left and the text on the right. By building it as a separate piece, we can easily change how *all* videos look by just editing that one file. 

When you type in the search bar, it instantly tells the "librarian" (the ViewModel from the previous task) what you typed. The librarian hands back the matching index cards, and the screen instantly redraws the list.

### Result

Micro-task `P1.S5.SS1.MT2` is complete.
Sprint 1.5 is **COMPLETE**.
The next sprint is **Sprint 1.6 — Settings Screen**.

## Settings Screen Implementation (2026-05-31)
The Settings Screen allows users to customize their experience and manage offline data.

### Components Built:
1. **Language Settings**: Radio buttons for Telugu, Urdu, Hindi, and English with bilingual labels.
2. **Islamic Source Preference**: Options for All Sources, Neutral Only, and Hanafi (Deoband).
3. **Offline Knowledge Database**: Cards for each source showing size, status, and an interactive download/update button.
4. **App Settings**: Toggles for "Auto-play read aloud" and "Large text mode".

### Files:
- `SettingsViewModel.kt`: Holds the state for all settings choices using StateFlow.
- `SettingsScreen.kt`: Uses LazyColumn to efficiently display settings sections with high touch targets (56dp).

### Verification:
Build successful with `./gradlew assembleDebug`. All components visually respond to user interaction.

---

## Phase 1 Completion Summary (2026-06-03)

Phase 1 has been successfully completed. The Android UI Skeleton is fully functional with four interconnected screens using the MVVM architecture.

### 📁 Final Project Structure (Layer 1)
- **`ui/screens/`**: `HomeScreen`, `AnswerScreen`, `VideoLibraryScreen`, `SettingsScreen`, and `NavGraph`.
- **`ui/components/`**: Reusable `MicButton`, `LanguageSelector`, `SourceSelector`, and `VideoCard`.
- **`ui/theme/`**: Centralized `Color.kt`, `Type.kt`, and `Theme.kt` (ensuring 16sp+ accessibility).
- **`viewmodel/`**: Dedicated ViewModels for all primary screens (`Home`, `Answer`, `VideoLibrary`, `Settings`).
- **`model/`**: Data models for `FatwaAnswer` and `RelatedVideo`.

### 📱 Screen Verification
1. **Home Screen**: Features a 40% height animated Mic button and bilingual language/source selectors.
2. **Answer Screen**: Displays detailed answers with clickable source URLs and a pinned "Read Aloud" button.
3. **Video Library**: Searchable list of lectures with dynamic real-time filtering.
4. **Settings Screen**: High-contrast toggles for app preferences and offline data management.

### ✅ Accessibility & Rules Audit
- [x] All font sizes ≥ 16sp.
- [x] All touch targets ≥ 48dp.
- [x] Every label is bilingual (Telugu + English).
- [x] MVVM pattern enforced via Hilt.
- [x] No hardcoded strings (all moved to `strings.xml`).

**Phase 1 is now locked. Ready to proceed to Phase 2: Backend Docker Services.**

## 🛠️ Phase 4 — Connect Android to Backend
**Status:** 🟡 IN PROGRESS

### 1. Network Layer Implementation (Sprint 4.1)
Established the communication bridge between Android and the FastAPI server.
- **ApiService.kt**: Defined the interface for `/health` and `/query` endpoints.
- **NetworkModels.kt**: Defined the `QueryRequest`, `ChatMessage`, and `QueryResponse` data classes.
- **NetworkModule.kt**: Configured Hilt to provide Retrofit with Gson converter and a shared OkHttpClient with 30s timeouts.

### 2. Repository Pattern (Sprint 4.1)
Implemented a clean data abstraction layer.
- **MainRepository.kt**: Injects `ApiService` and provides a `askQuestion` method.
- **Resource.kt**: A generic wrapper class to handle Success/Error/Loading states in the UI.

### 3. HomeViewModel Wiring (Sprint 4.2)
Connected the UI to the backend for the first time.
- **HomeViewModel.kt**: Now injects `MainRepository`.
- **Mic Button Action**: Toggling the mic now triggers a test query ("How to perform wudu?") to the server.
- **UI State Management**: Handles `Processing` and `Error` states from the repository.

**Verification:**
The project was successfully compiled with `./gradlew assembleDebug`. Hilt successfully injects the repository into the ViewModel.

---

## Multiple Source Support & Transparency (2026-06-09)

The app now supports displaying multiple Islamic sources for a single answer and provides transparency into the AI's search process.

### 1. Multi-Source Data Model (`AnswerModels.kt`)
- **`FatwaSource`**: A new data class to hold both the source name (e.g., "IslamQA.info") and its unique URL.
- **`FatwaAnswer` Refactor**: Updated the `sources` field to a `List<FatwaSource>`, allowing the app to credit all websites that contributed to the generated answer.

### 2. AI Search Transparency
- **Original Question Display**: The `AnswerScreen` now clearly shows the user's original question at the top to confirm what the AI is responding to.
- **Expanded Search Query**: The screen includes an "AI Search Logic" card. This provides a layman-friendly explanation of how the AI expanded the user's simple question with technical Islamic terms (e.g., "Wudu" -> "Ablution, Taharah") to find more accurate data in the vector database.

### 3. Repository Mapping (`MainRepository.kt`)
- The repository now processes the full list of source URLs from the backend.
- It dynamically assigns human-readable names to these URLs based on known Islamic databases (IslamQA.info, Darul Ifta Deoband, etc.).

---

## Global Madhab Filtering & User Preferences (2026-06-09)

Implemented a centralized preference system to allow users to filter answers based on their preferred school of thought (Madhab).

### 1. `UserPreferencesRepository.kt`
- Acts as the "Single Source of Truth" for app settings.
- Maps UI choices ("Hanafi", "Neutral") to backend-specific collection names ("deoband", "islamqa").
- Uses a Singleton pattern to ensure all screens (Home, Settings) stay in sync.

### 2. Guided Settings Experience
- **Layman Explanations**: The `SettingsScreen` now features informational "Lightbulb" cards (💡).
- These cards explain in simple, bilingual language (Telugu + English) what each choice means (e.g., "The Hanafi option focuses on rulings from the school of thought common in South Asia").

### 3. Integrated Home Screen Logic
- The `SourceSelector` chips on the Home screen are now synced with the global settings.
- When a user asks a question, the `HomeViewModel` fetches the user's preference and automatically restricts the backend search to the selected Madhab sources.

### 4. Code Quality & Beginner Guidance
- All new logic includes detailed, line-by-line comments.
- Uses stable internal "keys" (`all`, `neutral`, `hanafi`) to prevent bugs when switching between Telugu and English UI languages.

---

## Global Language Synchronization (2026-06-10)

Fixed the Home screen language selector so it reflects language changes made from the Settings screen.

### 1. Problem
- `SettingsViewModel.kt` and `HomeViewModel.kt` each kept their own separate `selectedLanguage` state.
- Selecting a language in Settings changed the app locale, but the Home selector still displayed its older local value.
- Language options were also duplicated in multiple files, which made future language changes risky.

### 2. Shared Language Model (`AppLanguage.kt`)
- Added `AppLanguage.kt` in `android-app/app/src/main/java/com/rushdululilm/app/model/`.
- Each supported language now has:
  - a stable app key such as `Telugu`
  - an Android locale tag such as `te`
  - a string resource ID for the display label
- To add or remove a language later, update `AppLanguage.kt` and the matching `strings.xml` resources.

### 3. Repository Single Source Of Truth
- `UserPreferencesRepository.kt` now owns `selectedLanguage` as a shared `StateFlow<AppLanguage>`.
- `updateLanguage()` updates both:
  - the shared StateFlow used by Home and Settings
  - the Android app locale through `AppCompatDelegate.setApplicationLocales()`
- This means both screens now read the same language value.

### 4. UI Refactor
- `LanguageSelector.kt` now accepts `AppLanguage` instead of raw strings.
- `SettingsScreen.kt` now builds its radio list from `AppLanguage.entries`.
- `HomeViewModel.kt` and `SettingsViewModel.kt` no longer duplicate language-to-locale mapping logic.

### 5. Build Environment Fix
- Removed old `-XX:MaxPermSize=256m` from `android-app/gradle.properties` because Java 8+ no longer supports PermGen.
- Added `org.gradle.java.home=/media/hidayat/PersonalData/Kali_Linux_Files/android-studio/panda4/jbr` so Gradle uses Android Studio's bundled Java runtime instead of system Java 25.

### Verification
Compiled successfully with:

```text
./gradlew :app:compileDebugKotlin
```

---

## Code Commenting Prompt System (2026-06-11)

Created a comprehensive AI Prompt system to enforce the project's strict line-by-line commenting guidelines across the entire codebase.

### 1. The Commenting Standard (Rule T2)
To accommodate Shaik Hidayatullah's profile as a beginner in Android Studio and Jetpack Compose, the codebase must adhere to the `Rule T2` commenting standard, where every single line of code is followed by a beginner-friendly comment using:
- `// ^` format in Kotlin files.
- `# ^` format in Python and Docker Compose files.

### 2. Prompt Creation
- Created `code_commenting_prompt.md` in the user's local artifacts directory.
- This file contains a copy-pasteable master prompt that instructs any coding AI (like Claude or Gemini) to systematically add detailed comments to target files without modifying their logic.
- It includes templates, rules, target file checklists, and verification instructions.

### 3. Safe Refactoring Workflow
The documentation details a safe step-by-step workflow:
1. Copy the master prompt template.
2. Select 1-2 files at a time.
3. Feed the prompt and files to the AI agent.
4. Replace the original file content with the commented version.
5. Verify build integrity using `./gradlew :app:compileDebugKotlin` from the `android-app/` directory.
6. Commit changes to Git once verified.

### 4. Codebase Commenting Completion (2026-06-11)
Successfully applied the beginner-friendly commenting rules (`Rule T1` and `Rule T2`) to all remaining screens in the Android App Layer and all Backend Services:
- **Android Screens**:
  - [AnswerScreen.kt](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/android-app/app/src/main/java/com/rushdululilm/app/ui/screens/AnswerScreen.kt): Added detailed comments for all Composable widgets, Annotated Strings, custom video card layouts, and state observers.
  - [SettingsScreen.kt](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/android-app/app/src/main/java/com/rushdululilm/app/ui/screens/SettingsScreen.kt): Added detailed comments for section cards, selectable group Columns, RadioButtons, Switch controls, and database Download items.
  - [VideoLibraryScreen.kt](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/android-app/app/src/main/java/com/rushdululilm/app/ui/screens/VideoLibraryScreen.kt): Added detailed comments for search OutlinedTextFields, clear/Search icons, and horizontal list cards.
- **Backend Services**:
  - [fastapi_server.py](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/fastapi_server.py): Commented REST endpoints, Pydantic models, and error handling.
  - [rag_pipeline.py](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/rag_pipeline.py): Commented LlamaIndex custom MultiCollectionRetriever, CombineQAPostprocessor, and NVIDIA NIM/Ollama LLM configurations.
  - [ingest_deoband.py](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/ingest_deoband.py) & [ingest_islamqa.py](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/ingest_islamqa.py): Commented SentenceTransformer loading, Qdrant collection recreation, SQLite batching, and payload structures.
  - [docker-compose.yml](file:///home/hidayat/Documents/Islamic-Knowledge-QA-App/backend/docker-compose.yml): Commented service volumes, environment variables, network bridges, and ports.

Build verified successfully using `./gradlew :app:compileDebugKotlin` in `android-app/` directory.


## Full Stack Smoke Test (2026-06-17)

Completed the end-to-end smoke test of the Android app with the FastAPI backend.

### 1. Networking Configurations
- Fixed the hardcoded `NetworkUtils.kt` LAN server detection to match the host IP (`192.168.0.102`).
- The app successfully transitions between "Offline", "LAN", and "Internet" tiers.

### 2. Emulation and Deployment
- Fixed a `minSdk` discrepancy (`minSdk = 31`) inside `build.gradle.kts` to allow installation on the API 31 `Pixel_5_API_31` emulator.
- Successfully built and installed the debug APK directly via the Android SDK ADB server.

### 3. Backend Verification
- Queried the Docker logs from `rushd_fastapi`.
- Verified successful `POST /query` requests at `/query` being processed and answered by the local API backend instance from the virtual device.
- All testing was fully off-cloud and processed exclusively through the offline LLM / RAG local instance.

