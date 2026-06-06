# AGENT_RULES.md
# Rushd-ul-Ilm (رشد العلم) — AI Agent Master Rules File
# Version 3.1 | Developer: Shaik Hidayatullah, Kurnool, Andhra Pradesh, India
# Updated: 2026-05-28 — Added sprint system integration
#
# ⚠️  MANDATORY FOR ALL AI AGENTS: Read this file COMPLETELY before writing a single line of code.
# ⚠️  After reading this file → read SPRINT_SYSTEM.md → read activity-logs/ACTIVITY_LOG.md → find CURRENT_MICRO_TASK → start there.
# ⚠️  Do NOT ask the developer to re-explain the project. Everything is in this file + SPRINT_SYSTEM.md + the PDF.

---

## 🔑 PROJECT IDENTITY

| Field | Value |
|-------|-------|
| App Name | Rushd-ul-Ilm (رشد العلم — Growth of Knowledge) |
| Package Name | `com.rushdululilm.app` ← NEVER change this |
| Developer | Shaik Hidayatullah |
| Location | Kurnool, Andhra Pradesh, India |
| Purpose | AI-powered Islamic Q&A for Telugu/Urdu speaking Muslims with low literacy |
| Primary Platform | Android (Kotlin + Jetpack Compose) |
| Future Platform | iOS (after Android version is complete) |
| System Design Version | v3.0 |

---

## 🚀 AGENT STARTUP CHECKLIST

Run through this checklist at the start of EVERY session. Check each item in order:

```
[ ] 1. Read this file (AGENT_RULES.md) completely
[ ] 2. Read SPRINT_SYSTEM.md — understand the micro-task system and Phase 1 breakdown
[ ] 3. Read activity-logs/ACTIVITY_LOG.md — find CURRENT_MICRO_TASK at the bottom
[ ] 4. Read Report Documentation/<file for current phase>.md
[ ] 5. If Mem0 is running on port 8100: query for "Rushd-ul-Ilm last session"
[ ] 6. If Neo4j/Graphiti is running on port 7687: query for project state
[ ] 7. Confirm to developer exactly:
        "Ready. Resuming micro-task [ID]: [one-sentence description].
         Previous session completed: [summary from ACTIVITY_LOG.md last entry].
         Blockers from last session: [None / description]."
[ ] 8. Begin ONLY the current micro-task — nothing more
```

**NEVER start coding before completing all 8 checklist steps.**

---

## 📐 COMPLETE TECHNOLOGY STACK

### Android App (runs on user's phone)

| Component | Technology | Version / Model | Notes |
|-----------|-----------|-----------------|-------|
| Language | Kotlin | 2.x | Primary language |
| UI Framework | Jetpack Compose | Latest BOM | No XML layouts allowed |
| Architecture | MVVM + StateFlow + Hilt DI | — | Hilt from day one |
| Local DB | Room (SQLite) | — | FTS5 for offline keyword search |
| HTTP Client | Retrofit + OkHttp | — | Calls FastAPI at port 8000 |
| Video Player | ExoPlayer / Media3 | Latest | Offline + streaming |
| Audio Capture | AudioRecord API | — | 16kHz mono PCM only |
| Network Check | ConnectivityManager | — | Ping-based tier detection |
| Offline STT | whisper.cpp via JNI | ggml-small-q5_0 (182MB) | Android NDK + CMake |
| Offline Translation | ONNX Runtime Android | 1.17.3 | Opus-MT ONNX INT8 models |
| Offline TTS | Android TextToSpeech | — | Google Telugu + Urdu packs |

### Server (Ubuntu machine with RTX 3050 → any Linux VPS)

| Service | Docker Image | Port | GPU | Purpose |
|---------|-------------|------|-----|---------|
| FastAPI | python:3.11-slim | 8000 | Optional | Main API — /transcribe /translate /tts /query |
| Ollama | ollama/ollama | 11434 | YES (CUDA) | Serves Qwen3:4b LLM |
| Qdrant | qdrant/qdrant | 6333 | No | Vector semantic search |
| IndicTrans2 | custom Python 3.11 | 8001 | YES (CUDA) | Telugu/Urdu ↔ English |
| Coqui XTTS-v2 | custom Python 3.11 | 8002 | YES (CUDA) | Text-to-Speech |
| faster-whisper | custom Python 3.11 | 8003 | YES (CUDA) | GPU speech recognition |
| Neo4j (Graphiti) | neo4j:5-community | 7474/7687 | No | Knowledge graph memory |
| Mem0 | custom Python 3.11 | 8100 | No | Cross-session agent memory |

**HARDWARE LIMIT: RTX 3050 = 4GB VRAM. Never load more than 3.5GB of models simultaneously.**

---

## 🕌 ISLAMIC SOURCE RULES — CRITICAL

### Approved Sources Only

| # | Website | URL | Madhab | Offline Copy? |
|---|---------|-----|--------|---------------|
| 1 | islamqa.info | https://islamqa.info/en | Neutral | YES — fast_mirror.py |
| 2 | islamqa.org | https://islamqa.org | Neutral | Planned |
| 3 | Darul Ifta Deoband | https://darulifta-deoband.com/en | Hanafi | YES — deoband_offline_downloader.py |
| 4 | Custom sources | User-configured | Any | PDFs, Obsidian .md, offline HTML |

### Islamic Content Rules (ABSOLUTE — NEVER VIOLATE)

```
RULE 1: NEVER allow the LLM to generate Islamic rulings from its own training data.
         The RAG prompt MUST say: "Answer ONLY from the provided context."

RULE 2: EVERY answer shown in the app MUST include the source URL as a clickable link.
         No source URL = the answer must NOT be displayed.

RULE 3: If no relevant context is found in Qdrant, the app shows:
         "I could not find an answer in the approved Islamic sources.
          Please consult a qualified Islamic scholar."
         — in the user's language (Telugu/Urdu), read aloud by TTS.

RULE 4: Do NOT add any new Islamic source without the developer's explicit permission.

RULE 5: Do NOT modify the RAG prompt to allow the LLM to use its own knowledge.
         This is a safety rule to prevent incorrect Islamic rulings.
```

---

## 📂 PROJECT FILE TREE (Update this when you add files)

```
rushd-ul-ilm/                          ← Project root
│
├── AGENT_RULES.md                      ← YOU ARE HERE — read this first
├── SPRINT_SYSTEM.md                    ← Read this second — full micro-task breakdown
├── INITIAL_CODEX_PROMPT.md             ← First-session bootstrap prompt for Codex TUI
├── REPORT_DOC_RULES.md                 ← Rules for Report Documentation folder
├── AI_SYSTEM_PROMPT.md                 ← System prompt for all AI agents
│
├── activity-logs/
│   └── ACTIVITY_LOG.md                 ← Session log — read this second
│
├── Report Documentation/               ← Detailed docs for each layer
│   ├── 01_PROJECT_OVERVIEW.md
│   ├── 02_ANDROID_APP_LAYER.md
│   ├── 03_STT_PIPELINE.md
│   ├── 04_TRANSLATION_PIPELINE.md
│   ├── 05_TTS_PIPELINE.md
│   ├── 06_RAG_PIPELINE.md
│   ├── 07_KNOWLEDGE_SOURCES.md
│   ├── 08_VIDEO_DATABASE.md
│   ├── 09_BACKEND_DOCKER.md
│   └── 10_OFFLINE_STRATEGY.md
│
├── knowledge-graph/
│   ├── GRAPHITI_SETUP.md               ← How to install and use Graphiti
│   └── MEM0_SETUP.md                   ← How to install and use Mem0
│
├── android-app/                        ← Android Studio project (Kotlin)
│   └── app/src/main/java/com/rushdululilm/app/
│       ├── ui/screens/                 ← Jetpack Compose screens
│       ├── ui/components/              ← Reusable Compose components
│       ├── viewmodel/                  ← MVVM ViewModels
│       ├── data/local/                 ← Room database entities + DAOs
│       ├── data/remote/                ← Retrofit API interface
│       ├── di/                         ← Hilt dependency injection modules
│       └── utils/                      ← Network check, audio helpers
│
├── backend/                            ← Ubuntu server Python code
│   ├── docker-compose.yml              ← Starts all services with one command
│   ├── fastapi_server.py               ← Main FastAPI app
│   ├── rag_pipeline.py                 ← LlamaIndex + Qdrant + Ollama
│   ├── stt_service.py                  ← faster-whisper endpoint
│   ├── translation_service.py          ← IndicTrans2 endpoint
│   └── tts_service.py                  ← Coqui XTTS-v2 endpoint
│
├── scraping/                           ← Islamic source download scripts
│   ├── fast_mirror.py                  ← islamqa.info scraper (EXISTS)
│   ├── dump_to_db.py                   ← islamqa.info → SQLite (EXISTS)
│   └── deoband_offline_downloader.py   ← Deoband scraper (EXISTS)
│
└── video-database/                     ← Islamic lecture indexing scripts
    ├── index_videos.py                 ← FFmpeg metadata extraction
    └── transcribe_videos.py            ← faster-whisper batch transcription
```

---

## 🔁 THREE-TIER OFFLINE FALLBACK — MANDATORY FOR ALL FEATURES

Every feature that touches the internet MUST implement all three tiers.
The app switches tiers automatically using ConnectivityManager + socket ping.

```
Tier 1 — Internet available:
  STT:         faster-whisper-large-v3-turbo GPU (port 8003) — ~1 second
  Translation: IndicTrans2 GPU (port 8001) — best accuracy
  TTS:         Coqui XTTS-v2 (port 8002) — natural voice
  Q&A:         LlamaIndex + Qdrant semantic + Qwen3:4b (ports 6333, 11434)
  Video search: Qdrant transcript search

Tier 2 — LAN only (same Wi-Fi as Ubuntu machine):
  All same as Tier 1 but via 192.168.x.x instead of cloud IP

Tier 3 — Fully offline (no network at all):
  STT:         whisper.cpp JNI (ggml-small-q5_0, 182MB, on-device) — ~8 sec
  Translation: Opus-MT ONNX INT8 (on-device, ~525MB total)
  TTS:         Android TextToSpeech API + Google voice packs (~50MB)
  Q&A:         Room FTS5 keyword search on downloaded SQLite fatwa DB
  Video search: Room FTS5 on downloaded video metadata SQLite DB
  Video play:  ExoPlayer from local phone storage
```

**Network detection code (use this exact pattern):**
```kotlin
// Check network tier in this order — always check LAN before internet
fun detectNetworkTier(context: Context): NetworkTier {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // Check if ANY network is available at all
    val network = cm.activeNetwork ?: return NetworkTier.OFFLINE
    val caps = cm.getNetworkCapabilities(network) ?: return NetworkTier.OFFLINE
    // Try to ping the local Ubuntu server (LAN check — 200ms timeout)
    return if (pingServer("192.168.1.100", 8000, timeoutMs = 200)) {
        NetworkTier.LAN  // Local server is reachable
    } else if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
        NetworkTier.INTERNET  // Internet is available, use cloud/VPS
    } else {
        NetworkTier.OFFLINE  // No network at all — use on-device models
    }
}

enum class NetworkTier { INTERNET, LAN, OFFLINE }
```

---

## 🎨 UI RULES (For Illiterate Users — NEVER VIOLATE)

```
1. Mic button: minimum 40% of screen height — it is the PRIMARY action
2. All labels: Telugu AND English — never English only
3. Touch targets: minimum 48dp for every button/interactive element
4. Font sizes: minimum 16sp for all user-facing text — no exceptions
5. Colors:
   - #2E7D32 (dark green) → Islamic answers, halal guidance
   - #1565C0 (calm blue) → Quranic references
   - #E65100 (orange) → Offline mode indicator banner
   - #B71C1C (red) → Errors only
6. "Read Aloud" button: ALWAYS visible on answer screen, minimum 56dp height
7. Offline banner: show at TOP of screen when Tier 3 is active:
   "📵 Offline Mode — Using Downloaded Knowledge"
8. Loading: always show progress with friendly text ("Searching Islamic sources...")
9. Error messages: translate to human-readable Telugu/Urdu — no HTTP codes to users
10. Answer layout: answer text → source name → [clickable source URL] → Read Aloud button
```

---

## 📝 CODE QUALITY RULES

```
RULE C1: Every code file begins with a header comment block:
         // File: [filename]
         // Purpose: [what this file does in one sentence]
         // Layer: [which of the 6 layers this belongs to]
         // Depends on: [other files this imports from]
         // Created: [date] | Modified: [date]
         // Developer: Shaik Hidayatullah

RULE C2: Every function has a docstring/KDoc comment explaining:
         - What it does
         - What each parameter means
         - What it returns
         - Any important edge cases

RULE C3: Every line of code has an inline comment (see Teaching Rule T2)

RULE C4: No magic numbers — every number is a named constant:
         ❌ Thread.sleep(200)
         ✅ val LAN_PING_TIMEOUT_MS = 200L; Thread.sleep(LAN_PING_TIMEOUT_MS)

RULE C5: All strings shown to users come from strings.xml (Android) — never hardcoded

RULE C6: All sensitive values (server IPs, API keys if any) go in local.properties
         or environment variables — NEVER hardcoded in source code

RULE C7: Error handling on every network call — no silent failures
```

---

## 🏃 SPRINT SYSTEM RULES — HOW TO EXECUTE TASKS

These rules govern how every AI agent breaks down and executes work. Read SPRINT_SYSTEM.md for the full micro-task breakdown.

```
RULE S1 — ONE MICRO-TASK PER RESPONSE
  Do exactly one micro-task per response. Never combine two micro-tasks.
  If the current micro-task finishes in under 5 minutes, proceed to the next
  one in the same session. If it takes over 40 minutes and is not done, STOP,
  split it into sub-tasks (MT3a, MT3b), document where you stopped,
  and update ACTIVITY_LOG.md with the partial completion.

RULE S2 — STATE THE MICRO-TASK ID FIRST
  Every response that does coding work begins with:
  "Starting micro-task [ID]: [one-sentence description from SPRINT_SYSTEM.md]"
  This lets the developer know exactly where in the plan we are.

RULE S3 — VERIFY DONE CONDITION BEFORE DECLARING COMPLETE
  Every micro-task in SPRINT_SYSTEM.md has a DONE CONDITION block.
  Read it. Verify every item in it is met before saying the task is complete.
  If a DONE CONDITION item fails — fix it before moving on. Never skip it.

RULE S4 — ASK BEFORE ADVANCING TO NEXT SPRINT
  You may advance automatically between micro-tasks within the same sub-sprint.
  But before starting a NEW sprint (e.g., moving from Sprint 1.2 to Sprint 1.3),
  stop and tell the developer:
  "Sprint [X.Y] is complete. All DONE CONDITIONS verified.
   Ready to begin Sprint [X.Y+1]: [sprint goal].
   First micro-task will be [ID]. Shall I continue?"
  Wait for confirmation before starting the new sprint.

RULE S5 — NEVER SKIP A MICRO-TASK
  Every micro-task exists for a reason. Never say "this one is obvious, I'll skip it."
  If a micro-task seems trivial, complete it quickly and document it.
  Skipped tasks create hidden gaps that cause failures in later phases.

RULE S6 — TEACH BEFORE CODE, EVERY TIME
  For every new concept introduced by a micro-task (a new Kotlin keyword,
  a new Android Studio panel, a new Gradle concept), write:
    - What it is in plain English (2-4 sentences)
    - A real-life analogy
    - THEN the code with every line commented
  The developer is a beginner. Teaching never stops.

RULE S7 — PHASE BOUNDARY CONFIRMATION
  Never begin Phase 2 without explicit developer confirmation that Phase 1 is complete.
  Same for all phase boundaries (2→3, 3→4, etc.).
  At each phase boundary, show a summary: "Phase [N] complete. X files created.
  Y micro-tasks completed. Verified working on emulator. Ready for Phase [N+1]?"
```

---

## 🛑 ABSOLUTE PROHIBITIONS (NEVER DO THESE)

```
❌ Change package name from com.rushdululilm.app
❌ Use any cloud LLM API (OpenAI, Anthropic, Google Gemini API) — ALL inference is local
❌ Add Firebase Analytics, Google Analytics, or any third-party telemetry
❌ Add Crashlytics, Sentry, or any crash reporter that sends data externally
❌ Generate Islamic rulings from LLM training data — RAG only
❌ Display any answer without a source URL
❌ Implement any feature without a three-tier offline fallback
❌ Suggest any AI model that needs more than 3.5GB VRAM
❌ Use XML layouts — Jetpack Compose only
❌ Add Islamic sources not in the approved list without developer permission
❌ Show raw error codes or stack traces to the user
❌ Use hardcoded strings in Kotlin UI code — always use strings.xml
```

---

## 📚 DOCUMENTATION RULE (EVERY MICRO-TASK — NO EXCEPTIONS)

At the end of every micro-task, you MUST update ALL of the following before telling the developer you are done:

**Step 1 — Update `activity-logs/ACTIVITY_LOG.md`** (append, never overwrite):
```
## Session [YYYY-MM-DD HH:MM]
AGENT: [Claude Code / Gemini-CLI / Codex / other]
PHASE: [1-6]
SPRINT: [e.g., 1.3]
SUB_SPRINT: [e.g., 1.3.2]
MICRO_TASK_COMPLETED: [e.g., P1.S3.SS2.MT2]
MICRO_TASK_DESCRIPTION: [one sentence]
SESSION_DURATION: [e.g., "35 minutes"]

FILES_CREATED:
  - [exact path] — [one sentence: what this file does]

FILES_MODIFIED:
  - [exact path] — [what changed and why]

DONE_CONDITION_MET: [YES / NO — if NO, explain why and what is left]

CURRENT_MICRO_TASK: [completed ID]
NEXT_MICRO_TASK: [next ID from SPRINT_SYSTEM.md]
NEXT_MICRO_TASK_DESCRIPTION: [one sentence from SPRINT_SYSTEM.md]

BLOCKERS: [None / description of any unsolved problem]
NOTES_FOR_NEXT_AGENT: [anything not in the code that the next agent needs to know]
GRAPHITI_UPDATED: [YES / NO / NOT RUNNING]
MEM0_UPDATED:     [YES / NO / NOT RUNNING]
```

**Step 2 — Update `SPRINT_SYSTEM.md` progress tracker:**
- Mark the completed micro-task with `[x]` in the Sprint Progress Tracker dashboard
- Update `CURRENT_MICRO_TASK` to the next task ID

**Step 3 — Update `Report Documentation/<relevant file>.md`:**
- Add the code written with full comments
- Add beginner-friendly explanation of what was built
- Update architecture diagram if structure changed

**Step 4 (if running) — Graphiti and Mem0:**
- Graphiti: `python knowledge-graph/graphiti_helper.py save-session "[description]"`
- Mem0: `python knowledge-graph/mem0_helper.py save "[description]"`

---

*Rushd-ul-Ilm (رشد العلم) — System Design v3.0 | Shaik Hidayatullah, Kurnool, India*
*Self-hosted | Privacy-first | Built for Telugu and Urdu speaking Muslims*
