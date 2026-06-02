# 01_PROJECT_OVERVIEW.md
# Rushd-ul-Ilm (رشد العلم) — Project Overview Documentation
# Last Updated: 2026-05-28 | Updated by: Claude (initial setup)
# Status: COMPLETE — Updated as project grows
# Changes: Initial documentation created from System Design PDF v3.0

---

## 🎯 What This App Does (Plain English)

Rushd-ul-Ilm is an Android app built for Muslims in Kurnool, Andhra Pradesh, India.
Many Muslims there cannot read or write English, but they speak Telugu and Urdu fluently.

**The app lets them:**
1. Press a big microphone button and ask an Islamic question in their own language (Telugu or Urdu)
2. The app converts their voice to text, translates it to English, and searches trusted Islamic websites
3. The app gets a cited answer (with the source website URL) and reads it back to the user in their language
4. The user can also browse offline Islamic video lectures from local storage

**No typing required. No English required. Fully voice-based.**

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
│  4 screens: Home (mic) → Answer → Video Library → Settings      │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP calls via Retrofit
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 2: AI / NLP Core (Ubuntu Server — Docker)                │
│  Converts voice → text → meaning → answer → voice              │
│  faster-whisper (STT) + IndicTrans2 (translate) + Coqui (TTS)  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 3: Islamic Knowledge Sources                             │
│  Scraped content from islamqa.info + Deoband stored in SQLite   │
│  Available as downloadable offline DB for the phone             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 4: Vector DB + RAG Pipeline                              │
│  Qdrant finds relevant fatwas → Qwen3:4b generates cited answer │
│  "Answer ONLY from provided context. Cite source URL."          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 5: Islamic Video Database                                │
│  Pre-downloaded Islamic lectures + Whisper transcripts          │
│  Searchable by topic — ExoPlayer plays them offline             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Layer 6: Self-Hosted Backend (Docker Compose)                  │
│  FastAPI + Ollama + Qdrant + IndicTrans2 + Coqui XTTS-v2        │
│  Runs on Ubuntu with GTX 1650ti → deployable to any Linux VPS   │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Why Each Technology Was Chosen

| Technology | Chosen Because | Alternative Rejected | Why Rejected |
|-----------|---------------|---------------------|--------------|
| Kotlin | Android's official language; best Android Studio support | Java | Older, more verbose; Kotlin is the future |
| Jetpack Compose | Modern Android UI; no XML needed; Google's standard since 2021 | XML layouts | Older approach; two systems to learn |
| Qwen3:4b | Fits in 4GB VRAM; 256K context; multilingual; good reasoning | Llama 3.1 8B | Too large for GTX 1650ti |
| Qdrant | Self-hosted Docker; fast; metadata filters; free | Pinecone, Weaviate | Cloud = user data leaves machine |
| LlamaIndex | Best RAG framework; native Qdrant + Ollama support | LangChain | More complex for our use case |
| IndicTrans2 | Best accuracy for Telugu/Urdu/Hindi ↔ English | Google Translate API | Sends user data to Google |
| Coqui XTTS-v2 | Natural voice; 30+ languages; self-hosted | Google TTS API | Privacy; Coqui has better Telugu quality |
| whisper.cpp JNI | Runs on phone with no internet; 99 languages | Google STT | Offline capability; privacy |
| Opus-MT ONNX | Runs on phone with no internet; ~525MB total | Nothing available | Only viable offline translation for Telugu |
| Room (SQLite) | Android standard; FTS5 for keyword search; offline | Realm, Firebase | Firebase = cloud; Room is built-in |
| FastAPI | Fastest Python API framework; easy to learn; async | Django, Flask | Flask too basic; Django too heavy |
| Docker Compose | Single command to start all services; portable | Manual installs | Reproducible; easy VPS migration |
| Neo4j + Graphiti | Temporal knowledge graph; open-source; self-hosted | Cloud memory services | Privacy; temporal awareness |
| Mem0 | Self-hostable; works with Ollama; cross-agent memory | Commercial memory APIs | Cost; privacy |

---

## 📋 Build Phase Summary

| Phase | What Gets Built | Est. Duration | Key Output |
|-------|----------------|---------------|------------|
| Phase 1 | Android UI Skeleton | 2-3 weeks | 4 working screens with placeholder data |
| Phase 2 | Backend Docker Services | 1-2 weeks | LLM answering questions at localhost:8000 |
| Phase 3 | Knowledge Ingestion | 2-3 weeks | Fatwas indexed in Qdrant with source URLs |
| Phase 4 | Android ↔ Backend | 2-3 weeks | Real voice Q&A with Islamic references |
| Phase 5 | Multilingual + Offline | 3-4 weeks | Telugu/Urdu voice + fully offline mode |
| Phase 6 | Video + Deployment | 2-3 weeks | Complete app deployed for Kurnool users |

---

## 🔒 Privacy Guarantees

```
What NEVER leaves the developer's infrastructure:
  ✅ User voice recordings — Whisper on Ubuntu or on-device
  ✅ Islamic questions — never sent to Google, OpenAI, or any cloud
  ✅ User query history — only in local Qdrant + SQLite
  ✅ LLM inference — Qwen3:4b via Ollama on the developer's GPU
  ✅ Translation — IndicTrans2 on GPU / Opus-MT on phone
  ✅ Zero telemetry — all tools (Ollama, Qdrant, FastAPI) have no telemetry
```

---

## 🔗 See Also
- 02_ANDROID_APP_LAYER.md — Kotlin + Jetpack Compose screens
- 09_BACKEND_DOCKER.md — Docker Compose setup
- 10_OFFLINE_STRATEGY.md — Three-tier offline fallback
