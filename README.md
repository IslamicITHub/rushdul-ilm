<div align="center">

# رشد العلم — Rushd-ul-Ilm

### *Growth of Knowledge*

**An Open-Source, AI-Powered Islamic Knowledge Q&A App**
**Built for Muslims with Low Digital Literacy**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Python](https://img.shields.io/badge/Python-3.11%2B%20%2F%203.13-blue.svg)](https://www.python.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![Privacy](https://img.shields.io/badge/Privacy-No%20Analytics-brightgreen.svg)](#-privacy-and-external-processing)
[![AI](https://img.shields.io/badge/AI-Local%20or%20NIM-orange.svg)](#-privacy-and-external-processing)

---

*Voice-first access to Islamic knowledge for Telugu and Urdu speakers.*

*Ask Islamic questions in Telugu, Urdu, or Hindi — review returned source links and use read-aloud support.*

</div>

---

## 📋 Table of Contents

- [The Problem](#-the-problem)
- [Our Solution](#-our-solution)
- [How It Works](#-how-it-works)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Network and Offline Status](#-network-and-offline-status)
- [Technology Stack](#-technology-stack)
- [Current Development Status](#-current-development-status)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Islamic Content Integrity](#-islamic-content-integrity)
- [Privacy and External Processing](#-privacy-and-external-processing)
- [Known Issues & Limitations](#-known-issues--limitations)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [Developer Notes](#-developer-notes)
- [License](#-license)
- [Acknowledgements](#-acknowledgements)

---

## 🎯 The Problem

Millions of Muslims in rural India — particularly in **Kurnool (Andhra Pradesh), Telangana, and surrounding regions** — face significant barriers to accessing authentic Islamic knowledge:

| Barrier | Impact |
|---------|--------|
| **Language** | Most authentic Islamic Q&A resources (like IslamQA.info) are in English or Arabic — not in Telugu or Urdu |
| **Literacy** | Many community members cannot read or write in any language — they communicate entirely through speech |
| **Digital Literacy** | Even those with smartphones struggle to type queries, navigate web browsers, or evaluate search results |
| **Misinformation** | Without access to authenticated sources, people rely on word-of-mouth, which can spread incorrect rulings |
| **Internet Access** | Reliable internet is not always available, making cloud-dependent apps unusable |

**The result:** A large population of sincere Muslims who *want* to learn about their religion but are **locked out** by technology, language, and literacy barriers.

---

## 💡 Our Solution

**Rushd-ul-Ilm** (رشد العلم — *Growth of Knowledge*) is a **voice-first Android app** that removes every barrier:

```
🎤 Press the big microphone button
🗣️ Ask your question in Telugu, Urdu, or Hindi
🔍 AI searches authenticated Islamic scholar databases
📖 Get a cited answer with source URLs
🔊 The app reads the answer aloud in your language
```

### What makes this different?

| Feature | Rushd-ul-Ilm | Typical Islamic Apps |
|---------|-------------|---------------------|
| Voice Input | ✅ Primary interface — no typing needed | ❌ Typing required |
| Local Languages | ✅ Telugu, Urdu, Hindi, English | ❌ Usually English/Arabic only |
| Source Citations | ✅ Displays source URLs returned by retrieval | ❌ Often uncited |
| Read Aloud | ✅ Backend TTS and Android offline fallback | ❌ Rare |
| Offline Mode | 🟡 Partial fallbacks; no complete three-tier pipeline | ❌ Internet-dependent |
| Privacy | ✅ No analytics found; local inference unless optional NIM is enabled | ❌ Undisclosed external data flows |
| Open Source | ✅ GPLv3 — community-driven | ❌ Proprietary |

---

## 🔄 How It Works

```mermaid
sequenceDiagram
    participant U as 👤 User
    participant App as 📱 Android App
    participant STT as 🎤 Speech-to-Text
    participant Trans as 🌐 Translation
    participant RAG as 🔍 RAG Pipeline
    participant LLM as 🤖 LLM
    participant TTS as 🔊 Text-to-Speech

    U->>App: Presses mic & speaks in Telugu/Urdu
    App->>STT: Audio (16kHz mono PCM)
    STT-->>App: Transcribed text (language depends on the selected path)
    App->>Trans: Translate to English
    Trans-->>App: English question
    App->>RAG: Search Islamic knowledge base
    RAG->>RAG: Query Expansion (add synonyms/context)
    RAG->>RAG: Search the configured Qdrant collections
    RAG->>LLM: Context + strict RAG prompt
    LLM-->>RAG: Generated answer
    RAG-->>App: Answer + source URLs returned by retrieval
    App->>Trans: Translate answer to Telugu/Urdu
    Trans-->>App: Translated answer
    App->>TTS: Generate speech audio
    TTS-->>App: Audio file
    App->>U: Display answer + Read aloud 🔊
```

---

## ✨ Key Features

### 🎤 Voice-First Interface
- **Giant microphone button** (40%+ of screen height) — designed for illiterate users
- Press once to ask, tap "Read Aloud" to hear the answer
- Minimum 48dp touch targets for all interactive elements
- Minimum 16sp font size for all text

### 🌍 Multilingual Support
- **Telugu** (తెలుగు), **Urdu** (اردو), **Hindi** (हिंदी), **English**
- Dynamic language switching in Settings
- Bilingual labels throughout (local language + English)
- Powered by **IndicTrans2** (state-of-the-art Indic translation model)

### 📚 Authenticated Islamic Knowledge
- Source SQLite databases contained **15,739 IslamQA.info** and **8,781 Deoband** fatwa rows during the 2026-11-18 audit; current Qdrant counts were unavailable
- Backend responses include source URLs when retrieval provides them; the app displays the returned URLs
- Madhab preference filtering (Hanafi / Neutral / All sources)
- AI strictly instructed: *"Answer ONLY from provided context. Never use your own knowledge."*

### 🧠 Smart AI Features
- **Query Expansion**: AI enhances vague questions with Islamic terminology before searching
- **Conversational Clarification**: The RAG prompt requests clarification for broad topics; this behavior has not been verified end-to-end
- **Multi-turn Chat**: The backend accepts chat history, but the current Android question flow does not populate it
- **AI Transparency**: Users can see the expanded search query the AI used

### 📵 Partial Offline Support
- **Intended three-tier design**: Internet → LAN → Fully Offline; server routing and offline Q&A/translation are incomplete
- On-device speech-to-text via **whisper.cpp** (182MB model, runs on phone CPU)
- On-device **Opus-MT ONNX translation is not implemented** (ONNX Runtime is only a declared dependency)
- Offline Q&A search is not implemented; Room stores saved answers but has no fatwa FTS5 table
- Android built-in **TextToSpeech** is used when the network-tier check reports offline
- Offline whisper.cpp is wired but currently configured to transcribe English only
- The video library is mock data; offline video playback is not wired

### 💾 Answer History
- All answers automatically saved to local Room database
- Previously saved answers can be browsed offline
- Dedicated "Answers" tab in bottom navigation

### 🔒 Privacy
- No analytics or third-party crash reporting was found in the app
- If `NVIDIA_API_KEY` is configured, the backend sends the question and retrieved context to NVIDIA NIM; otherwise it selects local Ollama
- Audio can be uploaded to the configured backend STT service; review [SECURITY.md](SECURITY.md) for the network boundary
- No Firebase, no Google Analytics, no third-party SDKs

---

## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│  Layer 1: Android App (Kotlin + Jetpack Compose)                 │
│  5 screens: Home (mic) → Answer → Answers History → Videos →     │
│  Settings                                                        │
└──────────────────────────┬───────────────────────────────────────┘
                           │ HTTP calls via Retrofit + OkHttp
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 2: AI/NLP Core (Ubuntu Server — mixed Compose/native)    │
│  faster-whisper (STT) → IndicTrans2 (translate) → Parler TTS   │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 3: Islamic Knowledge Sources                              │
│  islamqa.info + Darul Ifta Deoband → SQLite → Qdrant vectors    │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 4: RAG Pipeline (LlamaIndex + Qdrant + LLM)              │
│  Semantic search → Context assembly → LLM generation            │
│  "Answer ONLY from provided context. Cite source URL."          │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 5: Video Library prototype                                │
│  Mock app entries + yt-dlp metadata scraper; no playback pipeline │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│  Layer 6: Backend (Compose + native Ollama; optional cloud NIM)  │
│  FastAPI (8000) + native Ollama (11434) + Qdrant (6333) +        │
│  IndicTrans2 (8001) + TTS (8002) + STT (8003)                  │
└──────────────────────────────────────────────────────────────────┘
```

> 📐 For detailed architecture diagrams including data flow, MVVM patterns, and backend service interactions, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 📵 Network and Offline Status

The app has a network-tier detector and some offline fallbacks, but the complete three-tier strategy is not implemented. In particular, backend URLs are fixed to a LAN address, so INTERNET detection does not select a configurable VPS.

| Capability | Tier 1: Internet | Tier 2: LAN | Tier 3: Fully Offline |
|-----------|-----------------|-------------|----------------------|
| **Speech-to-Text** | faster-whisper service | Same configured backend | whisper.cpp JNI (English currently forced) |
| **Translation** | IndicTrans2 service (device placement needs verification) | Same configured backend | Not implemented |
| **Text-to-Speech** | Indic Parler TTS service | Same configured backend | Android TTS when tier reports offline |
| **Q&A Search** | FastAPI + Qdrant + configured LLM | Same configured backend | Not implemented; saved-answer history remains local |
| **Video Playback** | Not implemented | Not implemented | Not implemented |

Network-tier detection exists, but backend URLs are hard-coded to a LAN address; this does not route Internet-tier traffic to a configurable VPS.

---

## ⚙️ Technology Stack

### Android App (User's Phone)

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | Kotlin 2.x | Android's official modern language |
| UI Framework | Jetpack Compose | Declarative UI — no XML layouts |
| Architecture | MVVM + StateFlow + Hilt DI | Clean separation of concerns |
| Local DB | Room (SQLite) | Saved-answer history; no Android FTS5 fatwa index |
| HTTP Client | Retrofit + OkHttp | Backend API communication |
| Video screen | Jetpack Compose | Mock records only; playback is not implemented |
| Audio Capture | AudioRecord API | 16kHz mono PCM for STT |
| Offline STT | whisper.cpp via JNI (NDK) | On-device speech recognition |
| Offline TTS | Android TextToSpeech | Built-in speech synthesis |

### Backend Services (Ubuntu host; mixed Compose/native)

| Service | Port | GPU | Purpose |
|---------|------|-----|---------|
| FastAPI | 8000 | Optional | Main API gateway — `/query`, `/transcribe`, `/translate`, `/tts` |
| Ollama | 11434 | Host-dependent | Local LLM selected when `NVIDIA_API_KEY` is absent |
| Qdrant | 6333 | No | Vector database for semantic search |
| IndicTrans2 | 8001 | YES (CUDA) | Telugu/Urdu/Hindi ↔ English translation |
| Indic Parler TTS | 8002 | YES (CUDA) | Text-to-speech in Indian languages |
| faster-whisper | 8003 | YES (CUDA) | GPU speech-to-text |

### AI Models

| Model | Purpose | Size | VRAM |
|-------|---------|------|------|
| openai/gpt-oss-20b | Primary when `NVIDIA_API_KEY` is set (NVIDIA NIM) | Cloud | Not local VRAM |
| Qwen3:4b | Ollama selected when the NIM key is absent | Local model | Uses local GPU/CPU resources |
| Qwen3-Embedding-0.6B | Embeddings in ingestion and retrieval code (1,024 dimensions) | Local model | CPU for retrieval; ingestion scripts request CUDA |
| IndicTrans2 (8-bit quantized) | Translation | ~2 GB | ~1.5 GB |
| ai4bharat/indic-parler-tts | Text-to-speech | ~1.5 GB | ~1.8 GB |
| faster-whisper-large-v3-turbo | Speech-to-text | ~1.5 GB | ~1.5 GB |
| ggml-small-q5_1 (whisper.cpp) | Offline on-device STT; bundled in app assets | ~181 MiB | CPU only |

> ⚠️ **Hardware Limit**: The target GPU has 4GB VRAM. Source review did not establish a global scheduler that guarantees GPU-heavy services never overlap; validate concurrency on the target machine.

---

## 📊 Current Development Status

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| Phase 1 | Android UI Skeleton | Implemented | Five navigation destinations are present |
| Phase 2 | Backend Services | Implemented; runtime not verified here | Compose defines API, Qdrant, translation, TTS, and STT; Ollama is native |
| Phase 3 | Knowledge Ingestion | Scripts and source databases present | Source DB counts verified; current Qdrant collections unavailable to inspect |
| Phase 4 | Android ↔ Backend | Implemented for configured LAN endpoint | Answer history and network-tier detection are present |
| Phase 5 | Multilingual + Offline | Partial | Online service paths exist; offline translation and offline Q&A are not implemented |
| Phase 6 | Video Library + Deployment | Prototype started | URL metadata scraper exists; indexing, playback, and deployment remain |

> 📋 For detailed feature status, see [docs/DEVELOPMENT_STATUS.md](docs/DEVELOPMENT_STATUS.md).

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Panda 4 (2025.3.4+) with Kotlin 2.x support
- **Python** 3.11+
- **Docker** + Docker Compose
- **NVIDIA GPU** with 4GB+ VRAM and NVIDIA Container Toolkit
- **Linux OS** recommended (developed on Parrot OS)

### 1. Clone the Repository

```bash
git clone https://github.com/IslamicITHub/rushdul-ilm.git
cd rushdul-ilm
```

### 2. Set Up the Android App

```bash
# Open the android-app/ folder in Android Studio
# Set JAVA_HOME to Android Studio's bundled JBR:
export JAVA_HOME=/path/to/android-studio/jbr

# Build the debug APK
cd android-app
./gradlew assembleDebug
```

### 3. Set Up the Backend

```bash
# Install Ollama and pull the fallback LLM model
curl -fsSL https://ollama.com/install.sh | sh
ollama pull qwen3:4b

# Create environment file
cp backend/.env.example backend/.env
# Optionally set NVIDIA_API_KEY to enable NIM; add HF_TOKEN for gated model downloads

# Start the Compose-managed services (Ollama runs natively on the host if local inference is selected)
cd backend
docker compose up -d

# Verify services are running
curl http://localhost:8000/health
curl http://localhost:6333/
```

### 4. Ingest Islamic Knowledge

```bash
# Run from backend/ with the configured Python environment and local model files
cd backend
python ingest_islamqa.py    # Recreates the islamqa collection, then inserts records
python ingest_deoband.py    # Recreates the deoband collection, then inserts records
```

### 5. Test the Q&A Pipeline

```bash
curl -X POST http://localhost:8000/query \
  -H "Content-Type: application/json" \
  -d '{"question": "How to perform wudu?", "language": "en"}'
```

> A separate `docs/SETUP_GUIDE.md` is not present. These setup notes are provisional and rely on host-mounted environments and local model files.

---

## 📁 Project Structure

```
rushdul-ilm/
│
├── README.md                           ← You are here
├── LICENSE                             ← GPLv3
├── CONTRIBUTING.md                     ← How to contribute
├── AGENT_RULES.md                      ← AI agent coding rules
├── SPRINT_SYSTEM.md                    ← Micro-task breakdown
│
├── docs/                               ← GitHub documentation
│   ├── ARCHITECTURE.md                 ← System design & diagrams
│   └── DEVELOPMENT_STATUS.md           ← Current build progress
│
├── android-app/                        ← Android Studio project
│   └── app/src/main/java/com/rushdululilm/app/
│       ├── ui/screens/                 ← 5 Jetpack Compose screens
│       │   ├── HomeScreen.kt           ← Main mic button screen
│       │   ├── AnswerScreen.kt         ← Displays cited Islamic answer
│       │   ├── AnswersHistoryScreen.kt ← Past answers from Room DB
│       │   ├── VideoLibraryScreen.kt   ← Video lecture browser
│       │   ├── SettingsScreen.kt       ← Language, madhab, offline
│       │   ├── NavGraph.kt             ← Navigation controller
│       │   └── Routes.kt              ← Route string constants
│       ├── ui/components/              ← Reusable Compose components
│       │   ├── MicButton.kt            ← Animated microphone button
│       │   ├── LanguageSelector.kt     ← Language dropdown
│       │   ├── SourceSelector.kt       ← Islamic source filter chips
│       │   └── VideoCard.kt            ← Video list item card
│       ├── ui/theme/                   ← Material3 theming
│       ├── viewmodel/                  ← MVVM ViewModels
│       ├── model/                      ← Data classes & enums
│       ├── data/local/                 ← Room database (entities, DAOs)
│       ├── data/remote/                ← Retrofit API service
│       ├── data/repository/            ← Repository pattern
│       ├── di/                         ← Hilt dependency injection
│       └── utils/                      ← Network utils, audio helpers
│
├── backend/                            ← Python server code
│   ├── docker-compose.yml              ← Containerized services; Ollama runs natively
│   ├── fastapi_server.py               ← Main API gateway
│   ├── rag_pipeline.py                 ← LlamaIndex RAG engine
│   ├── translation_service.py          ← IndicTrans2 endpoint
│   ├── tts_service.py                  ← Parler TTS endpoint
│   ├── stt_service.py                  ← faster-whisper endpoint
│   ├── ingest_islamqa.py               ← IslamQA → Qdrant embeddings
│   └── ingest_deoband.py               ← Deoband → Qdrant embeddings
│
├── video-database/                     ← Video metadata prototype
│   ├── scrape_channel_urls.py          ← yt-dlp URL/title/ID metadata only
│   └── video_urls.txt                  ← Channel/video URL input list
│
├── Report Documentation/               ← Internal implementation reports
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
└── activity-logs/                      ← AI agent session logs
    └── ACTIVITY_LOG.md                 ← Detailed session history
```

---

## 🕌 Islamic Content Integrity

This project treats Islamic knowledge with the **highest level of responsibility**:

### Source Verification
The RAG pipeline retrieves from the configured approved-source collections; citations depend on the retrieved records and are not independently validated by the app:

| # | Source | URL | Madhab | Fatwas |
|---|--------|-----|--------|--------|
| 1 | IslamQA.info | https://islamqa.info/en | Neutral (no madhab bias) | 15,739 source SQLite rows at 2026-11-18 audit |
| 2 | IslamQA.org | https://islamqa.org | Neutral | Planned |
| 3 | Darul Ifta Deoband | https://darulifta-deoband.com/en | Hanafi | 8,781 source SQLite rows at 2026-11-18 audit |

### RAG-Only Policy (Prompt-Enforced)
```
⚠️ The RAG prompt instructs the model to answer only from retrieved context; this is prompt-level, not a technical guarantee
✅ Source URLs returned by the backend are displayed as links
⚠️ This is prompt-level behavior; the backend does not guarantee every answer has a valid source URL
⚠️ If no relevant answer is found, the prompt instructs the model to say:
   "I could not find an answer in the approved Islamic sources.
    Please consult a qualified Islamic scholar."
```

### Adding New Sources
New Islamic sources can **only** be added with **explicit approval** from the project maintainer. This prevents unauthorized or inauthentic content from entering the knowledge base.

---

## 🔒 Privacy and External Processing

```
✅ No analytics SDK, third-party crash reporter, or app telemetry was found
✅ Saved answer history is held in the local Android Room database
⚠️ With `NVIDIA_API_KEY` configured, the backend sends the question and retrieved source
   context to NVIDIA NIM (`openai/gpt-oss-20b`)
✅ Without that key, the backend selects local Ollama (`qwen3:4b`)
⚠️ NIM request failures do not automatically retry with Ollama
⚠️ Audio may be sent over the configured HTTP connection to backend STT
⚠️ Review [SECURITY.md](SECURITY.md) and configure the service boundary before deployment
```

---

## ⚠️ Known Issues & Limitations

| Issue | Status | Workaround |
|-------|--------|------------|
| 4GB VRAM limit | 🟡 Needs runtime validation | Current source does not establish a safe global scheduler for concurrent GPU workloads |
| Hindi translation can produce text repetition loops | ✅ Resolved | Pinned `transformers==4.45.2` for translation service |
| UserPreferencesRepository uses in-memory storage | 🟡 Open | Some settings do not persist across app restarts |
| whisper.cpp offline language setting | 🟡 Open | Model asset is bundled, but native code currently forces English |
| Android emulator ADB drops connection sometimes | 🟡 Open | Reconnect via `adb connect` |
| GPU and host-memory use under concurrent workloads | 🟡 Needs runtime validation | Validate Compose services and native Ollama together on the target hardware |
| Offline translation (Opus-MT ONNX) not implemented | 🟡 Open | Dependency exists without an app inference implementation |
| Offline fatwa search/Q&A not implemented | 🟡 Open | Room only stores saved answers; no local fatwa FTS table |
| Backend address is hard-coded | 🟡 Open | Retrofit and STT/TTS calls target a fixed LAN address |
| Translation device placement is inconsistent | 🟡 Needs verification | Code sets CUDA but moves model to CPU before placing inputs on the configured device |

---

## 🗺️ Roadmap

### Remaining work (Phases 5–6)
- [ ] Resolve and test IndicTrans2 runtime device placement
- [ ] Implement and test offline Telugu/Urdu translation
- [ ] Implement an approved offline fatwa search/Q&A path
- [ ] Complete video indexing, transcription, search, download, and playback
- [ ] Configure backend addressing and verify Internet/LAN behavior
- [ ] Verify production deployment and distribution requirements

### Future Plans
- [ ] iOS version (after Android is complete)
- [ ] Opus-MT ONNX for on-device translation
- [ ] Community-submitted Islamic sources (with moderation)
- [ ] Multiple regional language support (Kannada, Tamil, Malayalam)
- [ ] Mosque/community center deployment kits
- [ ] Arabic language support
- [ ] Bookmark & share answers
- [ ] Family sharing mode (multiple users on one device)

---

## 🤝 Contributing

We welcome contributions from developers, Islamic scholars, translators, and community members!

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Development setup instructions
- Coding standards (line-by-line commenting required!)
- Islamic content rules
- Privacy requirements
- Pull request workflow

### Quick Start for Contributors
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Follow the coding standards in `AGENT_RULES.md`
4. Ensure every line of code has a beginner-friendly comment
5. Submit a Pull Request with a clear description

### Areas Where Help is Needed
- 🌍 **Translators**: Help translate the app UI into more Indian languages
- 🕌 **Islamic Scholars**: Review and validate fatwa source accuracy
- 📱 **Android Developers**: Help with UI/UX improvements and accessibility
- 🐍 **Python Developers**: Help optimize the RAG pipeline and backend services
- 📝 **Documentation**: Help improve user guides and API documentation
- 🧪 **Testers**: Test on various Android devices and report issues

---

## 👨‍💻 Developer Notes

### For AI Agents Working on This Project
- Read `AGENT_RULES.md` **first** — it contains all project rules
- Read `SPRINT_SYSTEM.md` for the micro-task breakdown
- Read `activity-logs/ACTIVITY_LOG.md` for session history
- Every line of code **must** have a beginner-friendly comment
- The developer (Shaik Hidayatullah) is learning Android development — explain concepts!

### Build Configuration
```bash
# Android build requires Android Studio's bundled JDK
export JAVA_HOME=/path/to/android-studio/jbr

# Build the app
cd android-app && ./gradlew assembleDebug

# Backend services
cd backend && docker compose up -d
```

### Important Constants
- Android package name: `com.rushdululilm.app` (NEVER change)
- Minimum Android SDK: API 31 (Android 12)
- Target SDK: API 34 (Android 14)

### Backend Environment Variables
```env
NVIDIA_API_KEY=                           # Optional; set a valid key to enable cloud NIM (question/context are sent)
HF_TOKEN=your_huggingface_token           # Used for gated model downloads
```

---

## 📜 License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

You are free to use, modify, and distribute this software, but any derivative work must also be open source under the same license.

---

## 🙏 Acknowledgements

### Islamic Knowledge Sources
- [IslamQA.info](https://islamqa.info/en) — For their comprehensive fatwa database
- [Darul Ifta Deoband](https://darulifta-deoband.com/en) — For their Hanafi madhab fatwa database

### AI & Technology
- [Ollama](https://ollama.com) — Local LLM serving
- [Qdrant](https://qdrant.tech) — Vector database
- [LlamaIndex](https://www.llamaindex.ai/) — RAG framework
- [IndicTrans2](https://github.com/AI4Bharat/IndicTrans2) (AI4Bharat) — Indian language translation
- [Parler TTS](https://github.com/huggingface/parler-tts) / [AI4Bharat](https://github.com/AI4Bharat) — Indian language text-to-speech
- [whisper.cpp](https://github.com/ggerganov/whisper.cpp) — On-device speech recognition
- [faster-whisper](https://github.com/SYSTRAN/faster-whisper) — GPU speech recognition
- [NVIDIA NIM](https://build.nvidia.com/) — Cloud LLM inference API

### Developer
- **Shaik Hidayatullah** — Project creator & maintainer, Kurnool, Andhra Pradesh, India

---

<div align="center">

**بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيم**

*In the name of Allah, the Most Gracious, the Most Merciful*

*Built with ❤️ for the Muslim community of Kurnool and beyond*

**[⭐ Star this repo](https://github.com/IslamicITHub/rushdul-ilm) if you find this project beneficial!**

</div>

