# 📚 Feature Proposal: Answers History — Local Storage & "Answers" Section
### Rushd-ul-Ilm (رشد العلم) — Islamic Knowledge Q&A App
**Developer:** Shaik Hidayatullah | **Phase:** 4 | **Proposed By:** Antigravity (Claude Sonnet 4.6)
**Date:** 2026-06-12

---

> [!IMPORTANT]
> This is a **proposal document only**. No code has been written yet. Please review and give consent before implementation begins.

---

## 🎯 What Problem Are We Solving?

Right now, when the user asks a question by holding the mic button, the answer appears on the screen **only once**. The moment they press the back button, that answer is **gone forever** — lost from memory. This is a huge problem for:

- **Low-literacy users** who may need to re-read an answer multiple times
- **Slow readers** who didn't finish reading the first time
- **Users who want to share** an answer with family later
- **Offline situations** where re-querying isn't possible

---

## 🏗️ Proposed Solution: Two-Part Feature

### Part 1 — Auto-Save Answers to Local Storage (Room Database)
Every time the app gets an answer, it **automatically saves it** to a local Room database on the user's phone.

### Part 2 — New "Answers" Tab in Bottom Navigation Bar
A new dedicated **"Answers" section** (4th tab in the bottom nav bar) lets users browse all previously asked questions and tap any one to re-read it.

---

## 📐 Architecture Diagram — How Everything Connects

```
┌─────────────────────────────────────────────────────────────┐
│                   USER JOURNEY (FLOW)                        │
└─────────────────────────────────────────────────────────────┘

 [User holds mic] → [STT + RAG pipeline] → [Answer arrives]
         │                                        │
         │                               ┌────────▼────────┐
         │                               │ MainRepository  │
         │                               │  .askQuestion() │
         │                               └────────┬────────┘
         │                                        │
         │                               ┌────────▼────────────────────┐
         │                               │  🆕 Auto-Save Trigger        │
         │                               │  AnswerHistoryRepository     │
         │                               │  .saveAnswer(fatwaAnswer)    │
         │                               └────────┬────────────────────┘
         │                                        │
         │                       ┌────────────────▼──────────────────────┐
         │                       │  Room Database (SQLite on-device)      │
         │                       │                                        │
         │                       │  Table: saved_answers                  │
         │                       │  ┌──────────────────────────────────┐ │
         │                       │  │  id (auto)  │ TEXT               │ │
         │                       │  │  question   │ TEXT               │ │
         │                       │  │  answer     │ TEXT               │ │
         │                       │  │  sources    │ JSON TEXT          │ │
         │                       │  │  language   │ TEXT               │ │
         │                       │  │  savedAt    │ Long (timestamp)   │ │
         │                       │  │  isOffline  │ BOOLEAN            │ │
         │                       │  └──────────────────────────────────┘ │
         │                       └────────────────┬──────────────────────┘
         │                                        │ (reads from DB)
         │                               ┌────────▼────────────────────┐
         └─────────────────────────────► │  AnswerScreen.kt            │
                                         │  (displays current answer)  │
                                         └─────────────────────────────┘


┌─────────────────────────────────────────────────────────────┐
│              NEW "ANSWERS" TAB FLOW                          │
└─────────────────────────────────────────────────────────────┘

 User taps [Answers] tab in bottom nav
         │
         ▼
 [AnswersHistoryScreen.kt]  ←──── AnswersHistoryViewModel
         │                              │
         │  shows a scrollable list     │  reads from AnswerHistoryRepository
         │  of ALL past Q&A cards       │       │
         │                             ┌▼───────▼─────────────────┐
         │                             │  Room DAO                 │
         │                             │  getAllAnswers()           │
         │                             │  (sorted by newest first) │
         │                             └──────────────────────────┘
         │
  User taps on a past Q&A card
         │
         ▼
 [AnswerScreen.kt]  ←──── loads that specific saved answer
         │                (same screen as current, reused!)
         │
  User can tap [🔊 Read Aloud] to hear it again
  User can tap [Back] → returns to AnswersHistoryScreen
```

---

## 📂 Files That Will Be Created or Modified

### 🆕 NEW FILES (7 files):

| File | Location | Purpose |
|------|----------|---------|
| `SavedAnswer.kt` | `data/local/` | Room database **Entity** (the table schema) |
| `FatwaSourceConverter.kt` | `data/local/` | Room TypeConverter (converts List→JSON for storage) |
| `SavedAnswerDao.kt` | `data/local/` | **DAO** — database query methods (insert, getAll, delete) |
| `RushdulIlmDatabase.kt` | `data/local/` | Room **Database** — master entry point |
| `AnswerHistoryRepository.kt` | `data/repository/` | Connects DAO to ViewModel (data bridge) |
| `AnswersHistoryScreen.kt` | `ui/screens/` | 🆕 **New screen** — shows list of past Q&A |
| `AnswersHistoryViewModel.kt` | `viewmodel/` | Brain of the Answers History screen |

### ✏️ MODIFIED FILES (6 files):

| File | What Changes |
|------|-------------|
| `MainRepository.kt` | Inject & call `AnswerHistoryRepository.saveAnswer()` after every successful API response |
| `Routes.kt` | Add `const val ANSWERS_HISTORY = "answers_history"` route |
| `NavGraph.kt` | Add new "Answers" tab to bottom navigation bar + register the new screen route |
| `AppDatabase.kt` (new) | Created as `RushdulIlmDatabase.kt` |
| `AppModule.kt` (in di/) | Provide Room DB + DAO + AnswerHistoryRepository via Hilt |
| `app/build.gradle.kts` | Add Room dependencies (`room-runtime`, `room-ktx`, `room-compiler`) |
| `strings.xml` | Add Telugu + English string labels for the new Answers section |

---

## 🔄 Detailed Data Flow — Step by Step

### Flow A: Saving an Answer Automatically

```
Step 1: User releases mic button
        │
Step 2: HomeViewModel.sendTestQuery() calls MainRepository.askQuestion()
        │
Step 3: Backend returns FatwaAnswer object
        │
Step 4: MainRepository receives the FatwaAnswer
        │
        └──► NEW STEP: MainRepository calls
                  AnswerHistoryRepository.saveAnswer(fatwaAnswer)
                  │
                  └──► SavedAnswerDao.insertAnswer(savedAnswer)
                            │
                            └──► Room writes row to SQLite DB on device
                                 (automatically offline-safe!)
        │
Step 5: latestAnswer StateFlow updated → AnswerScreen opens (existing behavior)
        │
Step 6: AnswerScreen shows the answer to the user
```

### Flow B: User Browses Past Answers

```
Step 1: User taps the [📋 Answers] tab in bottom nav
        │
Step 2: AnswersHistoryScreen composable loads
        │
Step 3: AnswersHistoryViewModel.init {
          viewModelScope.launch {
            AnswerHistoryRepository.getAllAnswers()
              └──► SavedAnswerDao.getAllAnswers()  (Flow<List<SavedAnswer>>)
          }
        }
        │
Step 4: Screen shows a scrollable list of Q&A cards
        Each card shows:
        - Question text (truncated to 2 lines)
        - Date/time it was saved
        - Answer language (Telugu/Urdu/English)
        - Source website badge (e.g. IslamQA.info)
        │
Step 5: User taps a card
        │
Step 6: NavController.navigate(Routes.ANSWER + "?answerId={id}")
        │
Step 7: AnswerScreen loads the saved answer from Room DB
        (instead of the latest live answer)
        │
Step 8: User can Read Aloud, browse sources — all normal behavior
        │
Step 9: User presses [← Back] → returns to AnswersHistoryScreen
```

---

## 🗃️ Room Database Design — Beginner-Friendly Explanation

> **What is Room?**
> Room is Android's built-in local database library. Think of it like a spreadsheet app (like Excel) that lives inside the user's phone. We define "columns" (fields), and Room handles saving and reading rows automatically.

```kotlin
// This is what the "saved_answers" TABLE will look like:

@Entity(tableName = "saved_answers")
data class SavedAnswer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // ^ Auto-incrementing ID (like a row number) — Room assigns this automatically
    
    val questionText: String,   // The user's original question
    val answerText: String,     // The full fatwa answer text
    val sourcesJson: String,    // List of citation URLs stored as JSON string
    val language: String,       // "Telugu", "Urdu", "English"
    val savedAtTimestamp: Long, // Unix timestamp — milliseconds since 1970
    val isOfflineCache: Boolean // Was this answer from local DB or live server?
)
```

---

## 🎨 UI Design — AnswersHistoryScreen Layout

```
┌─────────────────────────────────────────┐
│  ← [App Logo]   📋 My Answers / నా జవాబులు │ ← TopAppBar
│                                         │
│  [Search bar — optional future feature] │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ 🟢 IslamQA.info                     ││
│  │ ప్రయాణంలో నమాజు ఎలా చేయాలి?...   ││ ← Question (2 lines)
│  │                                     ││
│  │ Jun 12, 2026 | Telugu               ││ ← Date + Language
│  └─────────────────────────────────────┘│
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ 🟢 Deoband                          ││
│  │ రోజా విరిచే కారణాలు ఏమిటి?...    ││
│  │                                     ││
│  │ Jun 11, 2026 | Telugu               ││
│  └─────────────────────────────────────┘│
│                                         │
│  [Empty State if no answers yet]        │
│  📭 No saved answers yet.               │
│     Ask your first question!            │
│                                         │
├────┬────────┬──────────┬────────────────┤
│ 🏠 │ 🎥     │ 📋       │ ⚙️             │
│Home│ Videos │ Answers  │ Settings       │ ← Bottom NavBar
└────┴────────┴──────────┴────────────────┘
```

---

## ✅ Three-Tier Offline Compliance (Rule AH3)

This feature is **100% offline-first** by design:

| Tier | Behavior |
|------|----------|
| **Tier 1 (Internet)** | Answers saved from live RAG server → stored in Room DB |
| **Tier 2 (LAN)** | Answers saved from LAN server → stored in Room DB |
| **Tier 3 (Fully Offline)** | Room DB still readable — ALL past answers available offline |

The Answers History screen works **even with airplane mode on** because it reads from the local Room database, not from the internet.

---

## 📦 New Gradle Dependencies Required

```kotlin
// In app/build.gradle.kts — Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")      // Coroutines/Flow support
kapt("androidx.room:room-compiler:2.6.1")            // Code generation
// Also need: Gson for JSON serialization of FatwaSource list
implementation("com.google.code.gson:gson:2.10.1")
```

> [!NOTE]
> `kapt` is the Kotlin annotation processor. Room uses it to auto-generate database code behind the scenes so we don't have to write it manually. Think of it like `pip install` running a setup script that auto-generates helper files.

---

## 🕒 Estimated Implementation Time

| Step | Task | Time Estimate |
|------|------|---------------|
| 1 | Room Entity + DAO + Database files | ~30 min |
| 2 | AnswerHistoryRepository | ~15 min |
| 3 | Hilt DI Module update | ~15 min |
| 4 | MainRepository auto-save hook | ~10 min |
| 5 | AnswersHistoryViewModel | ~20 min |
| 6 | AnswersHistoryScreen UI | ~45 min |
| 7 | NavGraph + Routes update | ~15 min |
| 8 | strings.xml bilingual labels | ~10 min |
| 9 | Compile + test | ~20 min |
| **Total** | | **~3 hours** |

---

## ⚠️ Important Notes

1. **No data will be lost** — this feature only ADDS new files and gently modifies existing ones
2. **The AnswerScreen is REUSED** — we don't need to create a new "detail view", we load the saved answer into the existing `AnswerScreen.kt`
3. **Swipe-to-delete** will be added so users can remove old answers they don't need
4. **Privacy** — all data stays on the user's phone. No sync, no cloud, no telemetry (Rule AH5 compliance)
5. The `kapt` annotation processor requires adding `kotlin("kapt")` plugin to `build.gradle.kts`

---

*Ready for developer approval. Type "Yes, implement it" to begin coding.*
