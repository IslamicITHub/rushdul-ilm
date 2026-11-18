# 01_PROJECT_OVERVIEW.md
# Rushd-ul-Ilm (رشد العلم) — Project Overview Documentation
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: IMPLEMENTATION SNAPSHOT — SEE LIMITATIONS BELOW
# Changes: Reconciled the overview with the inspected Android and backend code

---

## 🎯 What This App Does (Plain English)

Rushd-ul-Ilm is an Android app built for Muslims in Kurnool, Andhra Pradesh, India.
Many Muslims there cannot read or write English, but they speak Telugu and Urdu fluently.

**The app lets them:**
1. Press a big microphone button and ask an Islamic question in their own language (Telugu or Urdu)
2. The app converts their voice to text, translates it to English, and searches trusted Islamic websites
3. The app displays the generated answer and any source URLs returned by retrieval, then offers read-aloud support
4. The app includes a prototype video library; offline video browsing and playback are not implemented

The intended experience is voice-first and multilingual. Current language coverage and offline behavior are limited by the implementations described below.
The RAG prompt requests answers grounded in retrieved sources, but the code does not guarantee that every generated answer has a valid source URL.

---

## 👥 Who The App Is For

| User Type | Description | What They Can Do |
|-----------|-------------|-----------------|
| Illiterate Muslims | Cannot read/write English or Urdu | Voice only — press mic, hear answer |
| Telugu readers | Can read Telugu but not English | See answer text in Telugu |
| Urdu speakers | Speak Urdu but can't read Urdu script | Voice in Urdu, voice answer back |
| Urdu readers | Can read Urdu | Full text + voice experience |
| Basic phone users | Know WhatsApp, YouTube basics | Use all app features with minimal help |

---

## 🏗️ Six-Layer Architecture (Overview)

```
┌─────────────────────────────────────────────────────────────────┐
│  Layer 1: Android App (Kotlin + Jetpack Compose)                │
│  What the user sees and touches on their phone                  │
│  5 destinations: Home → Answer → Answers History → Videos → Settings │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP calls via Retrofit
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 2: AI / NLP Core (Ubuntu Server — Docker)                │
│  Online: faster-whisper + IndicTrans2 + Indic Parler TTS        │
│  Offline: partial whisper.cpp STT + Android TextToSpeech         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 3: Islamic Knowledge Sources                             │
│  Scraped content from islamqa.info + Deoband stored in SQLite   │
│  Source SQLite databases; no Android knowledge-DB downloader     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 4: Vector DB + RAG Pipeline                              │
│  Qdrant retrieves fatwas → configured NIM/Ollama LLM drafts answer │
│  "Answer ONLY from provided context. Cite source URL."          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 5: Video Library prototype                               │
│  Mock Android cards + one yt-dlp URL metadata script             │
│  Transcription, search, download, and playback are not wired     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 6: Self-Hosted Backend (Docker Compose)                  │
│  FastAPI + native Ollama + Qdrant + IndicTrans2 + Parler + STT  │
│  Compose mounts host virtual environments and local model files  │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Why Each Technology Was Chosen

| Technology | Chosen Because | Alternative Rejected | Why Rejected |
|-----------|---------------|---------------------|--------------|
| Kotlin | Android's official language; best Android Studio support | Java | Older, more verbose; Kotlin is the future |
| Jetpack Compose | Modern Android UI; no XML needed; Google's standard since 2021 | XML layouts | Older approach; two systems to learn |
| Qwen3:4b | Local LLM option in Ollama | — | Selected when NVIDIA_API_KEY is absent; not an automatic retry after NIM failure |
| Qdrant | Self-hosted vector storage with metadata filters | Pinecone, Weaviate | Avoids using a separate hosted vector database; optional NVIDIA NIM is a separate cloud boundary |
| LlamaIndex | Best RAG framework; native Qdrant + Ollama support | LangChain | More complex for our use case |
| IndicTrans2 | Best accuracy for Telugu/Urdu/Hindi ↔ English | Google Translate API | Sends user data to Google |
| Indic Parler TTS | Backend speech generation on port 8002 | Coqui XTTS-v2 (not used by current service) | Current `tts_service.py` implementation |
| whisper.cpp JNI | Runs on phone without internet; current native configuration forces English | Google STT | The bundled model's advertised language coverage is not the same as the configured app behavior |
| Qwen3-Embedding-0.6B | Local embeddings, 1,024 dimensions in ingestion scripts | MiniLM (older docs) | Matches current ingestion configuration |
| Android TextToSpeech | Offline speech output when device voice data is available | Backend Parler TTS | Implemented fallback in `AnswerViewModel` |
| Room (SQLite) | Stores saved answer history | FTS5 (not implemented in Android Room) | Current Room schema contains `saved_answers` only |
| FastAPI | Fastest Python API framework; easy to learn; async | Django, Flask | Flask too basic; Django too heavy |
| Docker Compose | Defines the backend service set | Manual installs | Current host-mounted environments and fixed LAN addresses mean VPS portability is not established |
| NVIDIA NIM | Optional cloud LLM selected when `NVIDIA_API_KEY` is configured | Local Ollama selected when the key is absent | The backend sends the prompt, question, and retrieved context to NIM; NIM failures do not auto-retry locally |

---

## 📋 Build Phase Summary

| Phase | What Gets Built | Est. Duration | Key Output |
|-------|----------------|---------------|------------|
| Phase 1 | Android UI Skeleton | 2-3 weeks | 4 working screens with placeholder data |
| Phase 2 | Backend Docker Services | 1-2 weeks | LLM answering questions at localhost:8000 |
| Phase 3 | Knowledge Ingestion | 2-3 weeks | Fatwas indexed in Qdrant with source URLs |
| Phase 4 | Android ↔ Backend | 2-3 weeks | Real voice Q&A with Islamic references |
| Phase 5 | Multilingual + Offline | In progress / partial | Online services exist; offline translation and offline Q&A are not implemented |
| Phase 6 | Video + Deployment | Prototype started | URL metadata scraper exists; indexing, playback, and deployment remain |

---

## 🔒 Privacy Guarantees

```
Privacy boundary in the current implementation:
  ✅ No analytics SDK, third-party crash reporter, or app telemetry was found
  ✅ Saved answer history is stored in the Android Room database
  ⚠️ If NVIDIA_API_KEY is configured, the backend sends the question and retrieved
     source context to NVIDIA NIM (openai/gpt-oss-20b) for generation
  ✅ If the key is absent, the backend selects local Ollama (qwen3:4b)
  ⚠️ The current code does not retry locally after a NIM request fails
  ⚠️ Audio may be sent over the configured local HTTP network to the STT service;
     review SECURITY.md before production deployment
```

---

## 🔗 See Also
- 02_ANDROID_APP_LAYER.md — Kotlin + Jetpack Compose screens
- 03_STT_PIPELINE.md — online and device speech recognition
- 04_TRANSLATION_PIPELINE.md — IndicTrans2 and translation limits
- 05_TTS_PIPELINE.md — Parler and Android TextToSpeech
- 06_RAG_PIPELINE.md — retrieval and language-model selection
- 07_KNOWLEDGE_SOURCES.md — source collections and ingestion
- 08_VIDEO_DATABASE.md — video prototype status
- 09_BACKEND_DOCKER.md — Docker Compose setup
- 10_OFFLINE_STRATEGY.md — actual offline coverage
