# 📊 Development Status — Rushd-ul-Ilm

> Last updated: November 18, 2026

> **Current code audit:** This snapshot supersedes the older milestone summaries below, which record prior progress claims and have not all been re-verified. Current task pointers also conflict: the latest activity-log entry points to Phase 6 work, while the sprint dashboard still names a Phase 5 micro-task. Maintainer reconciliation is required; no new task ID is inferred here.

---

## Current Status

| | |
|---|---|
| **Current Phase** | Phase pointer disputed; Phase 5 offline work remains incomplete and Phase 6 has a prototype |
| **Overall Progress** | Not reliably measurable from current code |
| **Active Sprint** | Not confirmed; activity log and sprint dashboard disagree |
| **Next Major Milestone** | Reconcile task pointer, then continue approved Phase 5/6 work |

### Verified Implementation Snapshot

| Area | Current code evidence |
|------|-----------------------|
| Android | Five navigation destinations; Room persists saved answers, not a local fatwa index |
| STT | faster-whisper service exists; whisper.cpp model is bundled, while native JNI currently forces English |
| Translation | IndicTrans2 service exists; its configured CUDA/CPU device placement needs runtime verification; no offline ONNX inference was found |
| TTS | Backend uses Indic Parler TTS; Android TextToSpeech is used by the offline path |
| RAG | Qdrant retrieval and prompt-based answer generation exist; NIM is selected when `NVIDIA_API_KEY` is present, otherwise local Ollama is selected; NIM failure does not automatically retry with Ollama |
| Ingestion | Source SQLite databases contained 15,739 IslamQA.info and 8,781 Deoband rows when checked; live Qdrant collection counts were unavailable |
| Video | Five mock app records and a channel-URL metadata scraper; no wired transcription, search, download, or playback pipeline |
| Networking | Tier detection exists, but app/service URLs use a fixed LAN endpoint rather than configurable Internet/LAN routing |

No Android build, backend runtime, or live service verification was performed for this documentation synchronization.

---

## Historical Milestone Notes

The following sections preserve earlier progress reports for context. Their implementation claims and percentages are historical, not authoritative descriptions of the current code. Use the **Verified Implementation Snapshot** above and the matching `Report Documentation/` files for the current state.

---

## Phase Progress

| Phase | Name | Status | Micro-tasks | Key Deliverable |
|:-----:|------|:------:|:-----------:|-----------------|
| 1 | Android UI Skeleton | ✅ Complete | 29/29 | 5 working screens with navigation, theming, accessibility |
| 2 | Backend Docker Services | ✅ Complete | 10/10 | FastAPI + Qdrant + Ollama containerized and tested |
| 3 | Knowledge Ingestion | Scripts/source DBs present | Historical counts | Source databases were counted; live Qdrant counts are unavailable |
| 4 | Connect Android to Backend | ✅ Complete | 6/6 + fixes | Full Q&A pipeline, answer history, network detection |
| 5 | Multilingual + Offline | 🟡 ~90% | ~11/12 + fixes | Translation, TTS, STT working; offline translation pending |
| 6 | Video Library + Deployment | ⬜ Not Started | 0/? | Video indexing, production deployment |

---

## Phase 1 — Android UI Skeleton ✅

**Completed: June 3, 2026**

All 5 app screens built with Jetpack Compose, full bottom navigation, Material3 theming, bilingual labels, and accessibility compliance.

| Sprint | Name | Status |
|--------|------|--------|
| 1.1 | Environment & Project Setup | ✅ 7/7 tasks |
| 1.2 | App Structure & Navigation | ✅ 6/6 tasks |
| 1.3 | Home Screen | ✅ 6/8 tasks |
| 1.4 | Answer Screen | ✅ 3/7 tasks |
| 1.5 | Video Library Screen | ✅ 2/5 tasks |
| 1.6 | Settings Screen | ✅ 1/5 tasks |
| 1.7 | String Resources & Theming | ✅ 2/2 tasks |
| 1.8 | Phase 1 Integration Test | ✅ 2/2 tasks |

### Screens Built
- **HomeScreen** — Giant animated mic button, language selector, source filter chips, offline banner
- **AnswerScreen** — Answer text, source badges, clickable URLs, AI transparency card, pinned Read Aloud button
- **AnswersHistoryScreen** — Room DB-powered list of past answers with date/time
- **VideoLibraryScreen** — Video cards with search bar filtering
- **SettingsScreen** — Language, Madhab preference, offline download cards, accessibility toggles

---

## Phase 2 — Backend Docker Services ✅

**Completed: June 2, 2026**

All backend services containerized and verified with GPU acceleration.

| Sprint | Name | Status |
|--------|------|--------|
| 2.1 | Docker + NVIDIA Toolkit | ✅ 2/2 tasks |
| 2.2 | docker-compose.yml | ✅ 1/1 tasks |
| 2.3 | Ollama + Qwen3:4b | ✅ 1/1 tasks |
| 2.4 | FastAPI Server Skeleton | ✅ 2/2 tasks |
| 2.5 | Qdrant Vector DB | ✅ 1/1 tasks |
| 2.6 | Wire FastAPI to Ollama | ✅ 1/1 tasks |
| 2.7 | Integration Test | ✅ 2/2 tasks |

### Services Running
- **FastAPI** (port 8000) — Main API gateway with `/query`, `/health` endpoints
- **Qdrant** (port 6333) — Vector database with 2 collections
- **Ollama** (native, port 11434) — Qwen3:4b local LLM

---

## Phase 3 — Knowledge Ingestion ✅

**Completed: June 8, 2026**

All Islamic knowledge sources scraped, processed, and embedded into vector collections.

| Sprint | Name | Status |
|--------|------|--------|
| 3.1 | Scraper Review & Test | ✅ 6/6 tasks |

### Knowledge Base Stats
| Collection | Source | Source DB rows | Current embedding config | Dimensions |
|-----------|--------|--------|-----------------|-----------|
| `islamqa` | islamqa.info | 15,739 | Qwen3-Embedding-0.6B | 1,024 |
| `deoband` | darulifta-deoband.com | 8,781 | Qwen3-Embedding-0.6B | 1,024 |
| **Total source rows** | | **24,520** | | |

These are local SQLite source counts recorded in the 2026-11-18 audit, not verified live Qdrant point counts.

### RAG Pipeline Features
- ✅ Multi-collection semantic search (searches both sources simultaneously)
- ✅ Query expansion (LLM rewrites questions with Islamic terminology)
- ✅ Conversational clarification (asks follow-up questions for vague queries)
- ✅ Backend chat-history input; current Android question flow does not populate it
- ✅ Source filtering (user can select Hanafi / Neutral / All)
- ✅ NVIDIA NIM (`openai/gpt-oss-20b`) when configured; otherwise local Ollama (`qwen3:4b`). No automatic NIM-error fallback.

---

## Phase 4 — Connect Android to Backend ✅

**Completed: June 17, 2026**

Android app fully connected to backend with live Q&A, answer history, and network detection.

| Sprint | Name | Status |
|--------|------|--------|
| 4.1 | Network Layer & Repository | ✅ 2/2 tasks |
| 4.2 | Wire Home Screen to Backend | ✅ 1/1 tasks + bug fixes |
| 4.3 | Display Real Answer | ✅ 1/1 tasks + enhancements |
| 4.4 | Network Tier Detection | ✅ 1/1 tasks |
| 4.5 | Phase 4 Integration Test | ✅ 1/1 tasks |

### Features Delivered
- ✅ Retrofit + OkHttp network layer
- ✅ Hilt-injected repository pattern
- ✅ Real-time network tier detection (Internet/LAN/Offline)
- ✅ Offline banner with automatic "Back Online" notification
- ✅ Multiple source display with named badges
- ✅ AI transparency card showing expanded search query
- ✅ Global Madhab preference filtering
- ✅ Dynamic language switching (Telugu, Urdu, Hindi, English)
- ✅ Room database for answer history with auto-save
- ✅ Answer history screen with navigable saved answers
- ✅ CLEARTEXT HTTP fix for emulator development
- ✅ Null-safety hardening for server responses

---

## Phase 5 — Multilingual + Offline 🟡

**In Progress: June 17 — present**

Translation, TTS, and STT services exist. Earlier sprint notes claimed dynamic GPU offloading; current source does not establish a global scheduler, and translation device placement needs verification.

| Sprint | Name | Status |
|--------|------|--------|
| 5.1 | IndicTrans2 Translation | ✅ 4/4 tasks + bug fixes |
| 5.2 | TTS Service | ✅ 3/3 tasks |
| 5.3 | STT Service | ✅ 4/4 tasks |
| 5.4 | Offline Fallbacks | 🟡 Partially done |

### What's Working
- ✅ IndicTrans2 translation (English ↔ Telugu/Urdu/Hindi) with 8-bit quantization (~1.5GB VRAM)
- ✅ End-to-end Telugu/Hindi Q&A pipeline (ask in Telugu → get Telugu answer with citations)
- ✅ Indic Parler TTS generating speech in Indian languages
- ✅ faster-whisper GPU STT for online speech recognition
- ⚠️ GPU memory handling is service-specific; concurrency and the translation device path need runtime validation
- ✅ Android app plays TTS audio via MediaPlayer
- ✅ Offline TTS fallback using Android TextToSpeech API
- ✅ whisper.cpp JNI bridge for offline STT (C++ compiled, Kotlin JNI interface ready)
- ✅ Audio capture (16kHz mono PCM) via AudioRecorderHelper
- ✅ Virtual environment isolation (separate venvs for translation, TTS, STT to avoid dependency conflicts)
- ✅ Lazy model loading to reduce idle RAM usage
- ✅ STT CUDA library configuration resolved

### What's Pending
- ⬜ Opus-MT ONNX for fully offline on-device translation
- ✅ whisper.cpp model asset is bundled; native configuration currently forces English
- ⬜ End-to-end offline STT → offline search → offline answer pipeline testing

### Bug Fixes Applied in Phase 5
| Bug | Root Cause | Fix |
|-----|-----------|-----|
| Hindi text repetition loop | `transformers 5.x` incompatible with IndicTrans2 cache | Pinned `transformers==4.45.2` in isolated venv |
| CUDA OOM on translation | `num_beams=5` caused KV cache VRAM spikes | Reduced to `num_beams=2` |
| FastAPI + translation dependency conflict | Shared venv had incompatible transformers versions | Separate virtual environments per service |
| NVIDIA NIM model history | Older notes mention a Llama model switch | Current configured NIM model is `openai/gpt-oss-20b` |
| Float16 audio incompatibility | scipy can't write Float16 WAV | Convert to Float32 before disk write |
| STT missing CUDA libs | `nvidia-cublas-cu12` not installed in venv | Installed CUDA libraries, set LD_LIBRARY_PATH |

---

## Phase 6 — Video Library + Deployment ⬜

**Not Started**

| Planned Feature | Description |
|----------------|-------------|
| Video scraping | Download YouTube Islamic lecture metadata using yt-dlp |
| Video transcription | Batch transcribe lectures with faster-whisper |
| Video indexing | Embed transcripts into Qdrant for semantic search |
| Video playback | ExoPlayer integration for streaming + offline |
| Production deployment | Docker Compose on Linux VPS |
| App distribution | Google Play Store listing |

---

## Known Issues

| # | Issue | Severity | Status | Details |
|---|-------|----------|--------|---------|
| 1 | VRAM constraint (4GB) | Medium | 🟡 Needs runtime validation | No global scheduler guarantees services cannot overlap |
| 2 | UserPreferencesRepository in-memory | Low | 🟡 Open | Settings don't persist across app restarts; needs DataStore migration |
| 3 | whisper.cpp forces English offline | Medium | 🟡 Open | Model is bundled, but native configuration forces English |
| 4 | Concurrent backend memory use | Medium | 🟡 Needs runtime validation | Measure Compose services and native Ollama together on target hardware |
| 5 | Emulator ADB connection unstable | Low | 🟡 Open | Headless emulator drops ADB after deployment |
| 6 | Specific JAVA_HOME required | Low | 🟡 Open | Must point to Android Studio bundled JBR (Java 21) |
| 7 | Offline translation not yet available | Medium | 🟡 Planned | Opus-MT ONNX implementation deferred to Phase 6 |
| 8 | IndicTrans2 Urdu translation quality | Low | 🟡 Open | Urdu translation less accurate than Telugu; may need fine-tuning |

---

## Development Timeline

```mermaid
gantt
    title Rushd-ul-Ilm Development Timeline
    dateFormat YYYY-MM-DD
    axisFormat %b %d

    section Phase 1
    Android UI Skeleton       :done, p1, 2026-05-28, 2026-06-03

    section Phase 2
    Backend Docker Services   :done, p2, 2026-06-02, 2026-06-02

    section Phase 3
    Knowledge Ingestion       :done, p3, 2026-06-02, 2026-06-08

    section Phase 4
    Android ↔ Backend         :done, p4, 2026-06-08, 2026-06-17

    section Phase 5
    Multilingual + Offline    :active, p5, 2026-06-17, 2026-07-15

    section Phase 6
    Video + Deployment        :p6, 2026-07-15, 2026-08-15
```

---

## Agent Contributions

This project uses multiple AI coding agents. Here is a summary of their contributions:

| Agent | Sessions | Primary Contributions |
|-------|----------|----------------------|
| Gemini CLI | ~25 | UI screens, theming, backend setup, RAG pipeline, ingestion |
| Codex (GPT-5) | ~5 | Environment setup, emulator, Mem0 helper |
| Antigravity (Gemini) | ~10 | Code commenting, TTS/STT integration, GPU offloading, history feature |

---

> **Navigation**: [← Architecture](ARCHITECTURE.md) | [↑ README](../README.md) | [→ Contributing](../CONTRIBUTING.md)

