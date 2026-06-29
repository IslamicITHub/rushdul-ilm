<![CDATA[# 🏗️ System Architecture — Rushd-ul-Ilm

> Detailed technical architecture documentation for developers and contributors.

---

## Table of Contents

- [System Overview](#system-overview)
- [Six-Layer Architecture](#six-layer-architecture)
- [Three-Tier Offline Strategy](#three-tier-offline-strategy)
- [Android App Architecture (MVVM)](#android-app-architecture-mvvm)
- [Backend Services (Docker Compose)](#backend-services-docker-compose)
- [RAG Pipeline Flow](#rag-pipeline-flow)
- [End-to-End Data Flow](#end-to-end-data-flow)
- [Dynamic GPU Offloading](#dynamic-gpu-offloading)
- [Technology Decision Matrix](#technology-decision-matrix)
- [Network Tier Detection](#network-tier-detection)

---

## System Overview

```mermaid
graph TB
    subgraph "📱 User's Android Phone"
        UI["Jetpack Compose UI<br/>5 Screens"]
        VM["ViewModels<br/>(MVVM + Hilt DI)"]
        ROOM["Room DB<br/>(SQLite + FTS5)"]
        WHISPER_LOCAL["whisper.cpp JNI<br/>(Offline STT)"]
        ANDROID_TTS["Android TTS API<br/>(Offline TTS)"]
    end

    subgraph "🖥️ Ubuntu Server (Docker Compose)"
        FASTAPI["FastAPI Gateway<br/>:8000"]
        OLLAMA["Ollama (Qwen3:4b)<br/>:11434"]
        QDRANT["Qdrant Vector DB<br/>:6333"]
        INDICTRANS["IndicTrans2<br/>:8001"]
        TTS_SVC["Indic Parler TTS<br/>:8002"]
        STT_SVC["faster-whisper<br/>:8003"]
        NIM["NVIDIA NIM API<br/>(Llama 3.3 70B)"]
    end

    subgraph "📚 Knowledge Base"
        ISLAMQA["IslamQA.info<br/>15,739 fatwas"]
        DEOBAND["Darul Ifta Deoband<br/>8,781 fatwas"]
    end

    UI --> VM
    VM --> ROOM
    VM --> WHISPER_LOCAL
    VM --> ANDROID_TTS
    VM -->|"Retrofit + OkHttp"| FASTAPI
    FASTAPI --> OLLAMA
    FASTAPI --> QDRANT
    FASTAPI --> INDICTRANS
    FASTAPI --> TTS_SVC
    FASTAPI --> STT_SVC
    FASTAPI -->|"Cloud API"| NIM
    ISLAMQA -->|"Embedded vectors"| QDRANT
    DEOBAND -->|"Embedded vectors"| QDRANT
```

---

## Six-Layer Architecture

### Layer 1 — Android App (User Interface)
- **Technology**: Kotlin 2.x + Jetpack Compose (no XML)
- **Architecture**: MVVM + StateFlow + Hilt Dependency Injection
- **5 Screens**: Home (mic), Answer, Answers History, Video Library, Settings
- **Key Design**: Voice-first, 40%+ screen mic button, 48dp+ touch targets, bilingual labels

### Layer 2 — AI/NLP Core
- **STT**: faster-whisper (GPU) or whisper.cpp (on-device)
- **Translation**: IndicTrans2 (GPU, 8-bit quantized) — Telugu/Urdu/Hindi ↔ English
- **TTS**: Indic Parler TTS (GPU) or Android TTS API (offline)
- **LLM**: NVIDIA NIM (Llama 3.3 70B primary) + Ollama Qwen3:4b (fallback)

### Layer 3 — Islamic Knowledge Sources
- Scraped and structured from authenticated Islamic scholar websites
- Stored in SQLite databases with metadata (title, question, answer, URL)
- Converted to vector embeddings for semantic search

### Layer 4 — RAG Pipeline
- **Framework**: LlamaIndex
- **Retrieval**: Multi-collection Qdrant semantic search
- **Generation**: Strict prompt — answers only from provided context with source citations
- **Enhancement**: Query expansion, conversational clarification, chat history

### Layer 5 — Islamic Video Database (Planned)
- YouTube Islamic lectures indexed with whisper transcripts
- Searchable by topic via Qdrant
- Offline playback via ExoPlayer

### Layer 6 — Self-Hosted Backend
- Docker Compose orchestration
- All services on a single Ubuntu machine with RTX 3050 GPU
- Portable to any Linux VPS with Docker support

---

## Three-Tier Offline Strategy

```mermaid
graph LR
    subgraph "Tier 1: Internet"
        I_STT["faster-whisper GPU<br/>~1 second"]
        I_TRANS["IndicTrans2 GPU<br/>Best accuracy"]
        I_TTS["Indic Parler TTS<br/>Natural voice"]
        I_RAG["Qdrant + LLM<br/>Full semantic search"]
    end

    subgraph "Tier 2: LAN Only"
        L_STT["Same as Tier 1<br/>via 192.168.x.x"]
        L_TRANS["Same as Tier 1<br/>via local IP"]
        L_TTS["Same as Tier 1<br/>via local IP"]
        L_RAG["Same as Tier 1<br/>via local IP"]
    end

    subgraph "Tier 3: Fully Offline"
        O_STT["whisper.cpp JNI<br/>On-device ~8s"]
        O_TRANS["Opus-MT ONNX<br/>On-device (planned)"]
        O_TTS["Android TTS API<br/>Google voice packs"]
        O_RAG["Room FTS5<br/>Keyword search"]
    end

    DETECT["NetworkUtils<br/>detectNetworkTier()"] --> I_STT
    DETECT --> L_STT
    DETECT --> O_STT
```

### How Network Detection Works
1. `ConnectivityManager.NetworkCallback` provides a reactive stream of connectivity changes
2. The app attempts to ping the local Ubuntu server (192.168.x.x:8000) with 200ms timeout
3. If the LAN server responds → **Tier 2 (LAN)**
4. If not, check `NET_CAPABILITY_INTERNET` → **Tier 1 (Internet)**
5. Otherwise → **Tier 3 (Offline)**

The app shows a visual banner:
- 🟢 No banner when online
- 🟠 `"📵 Offline Mode — Using Downloaded Knowledge"` when Tier 3 is active
- 🟢 Brief `"Back Online!"` notification when connectivity restores

---

## Android App Architecture (MVVM)

```mermaid
graph TB
    subgraph "UI Layer (Jetpack Compose)"
        HS["HomeScreen"]
        AS["AnswerScreen"]
        AHS["AnswersHistoryScreen"]
        VS["VideoLibraryScreen"]
        SS["SettingsScreen"]
    end

    subgraph "ViewModel Layer"
        HVM["HomeViewModel"]
        AVM["AnswerViewModel"]
        AHVM["AnswersHistoryViewModel"]
        VLVM["VideoLibraryViewModel"]
        SVM["SettingsViewModel"]
    end

    subgraph "Repository Layer"
        MR["MainRepository"]
        AHR["AnswerHistoryRepository"]
        UPR["UserPreferencesRepository"]
    end

    subgraph "Data Sources"
        API["ApiService<br/>(Retrofit)"]
        DAO["SavedAnswerDao<br/>(Room)"]
        DB["RushdulIlmDatabase<br/>(Room)"]
        NET["NetworkUtils<br/>(ConnectivityManager)"]
    end

    subgraph "DI (Hilt)"
        NM["NetworkModule"]
        DBM["DatabaseModule"]
    end

    HS --> HVM
    AS --> AVM
    AHS --> AHVM
    VS --> VLVM
    SS --> SVM

    HVM --> MR
    HVM --> UPR
    AVM --> MR
    AVM --> AHR
    AHVM --> AHR
    SVM --> UPR

    MR --> API
    MR --> AHR
    AHR --> DAO
    DAO --> DB

    NM -->|"Provides"| API
    DBM -->|"Provides"| DB
    DBM -->|"Provides"| DAO
```

### Key Patterns
- **StateFlow**: All UI state is exposed via `StateFlow` from ViewModels
- **Sealed Classes**: `HomeUiState` uses sealed classes (Idle, Recording, Processing, NavigatingToAnswer, Error)
- **Repository Pattern**: ViewModels never directly access network or database
- **Hilt DI**: All dependencies injected at construction time
- **Resource wrapper**: Network calls return `Resource<T>` (Success, Error, Loading)

---

## Backend Services (Docker Compose)

```mermaid
graph TB
    subgraph "Docker Compose Network"
        FAST["fastapi<br/>Port 8000<br/>Host networking"]
        QD["qdrant<br/>Port 6333, 6334<br/>Bridge networking"]
        IT["indictrans2<br/>Port 8001<br/>GPU: NVIDIA"]
        TTS["tts<br/>Port 8002<br/>GPU: NVIDIA"]
        STT["stt<br/>Port 8003<br/>GPU: NVIDIA"]
    end

    OLL["ollama (native)<br/>Port 11434<br/>Running on host"]

    FAST -->|"localhost:11434"| OLL
    FAST -->|"localhost:6333"| QD
    FAST -->|"localhost:8001"| IT
    FAST -->|"localhost:8002"| TTS
    FAST -->|"localhost:8003"| STT

    PHONE["📱 Android App"] -->|"Retrofit HTTP"| FAST
```

### Service Details

| Service | Docker Image | Volumes | GPU | Notes |
|---------|-------------|---------|-----|-------|
| **fastapi** | Custom (python:3.11-slim) | `./:/app`, local_models, data folders | No | Host networking for Ollama access |
| **qdrant** | qdrant/qdrant:latest | qdrant_data (named) | No | Persistent vector storage |
| **indictrans2** | Custom (python:3.11) | venv_indictrans2, hf_models_data | NVIDIA | 8-bit quantized models |
| **tts** | Custom (python:3.11) | venv_tts, local_models | NVIDIA | Float16 model loading |
| **stt** | Custom (python:3.11) | venv_stt | NVIDIA | faster-whisper-large-v3-turbo |
| **ollama** | Native (not Docker) | ~/.ollama | NVIDIA | Runs on host, not in container |

---

## RAG Pipeline Flow

```mermaid
sequenceDiagram
    participant App as Android App
    participant API as FastAPI (/query)
    participant Trans as IndicTrans2 (:8001)
    participant RAG as RagPipeline
    participant QD as Qdrant (:6333)
    participant LLM as LLM (NIM/Ollama)

    App->>API: POST /query {question, language, sources, chat_history}

    alt language != "en"
        API->>Trans: Translate question to English
        Trans-->>API: English question
    end

    API->>RAG: ask(english_question, sources, chat_history)

    Note over RAG: Query Expansion
    RAG->>LLM: "Rewrite this question with Islamic terminology..."
    LLM-->>RAG: Expanded search query

    Note over RAG: Multi-Collection Search
    RAG->>QD: Search "islamqa" collection (top 5)
    RAG->>QD: Search "deoband" collection (top 5)
    QD-->>RAG: Combined results sorted by similarity

    Note over RAG: Context Assembly
    RAG->>RAG: Build context with source URLs

    Note over RAG: LLM Generation (Strict RAG Prompt)
    RAG->>LLM: Context + "Answer ONLY from provided context. Cite URLs."
    LLM-->>RAG: Generated answer with citations

    RAG-->>API: {answer, source_urls, expanded_query}

    alt language != "en"
        API->>Trans: Translate answer to user's language
        Trans-->>API: Translated answer
    end

    API-->>App: {answer, source_urls, question, expanded_query}
```

### RAG Safety Prompt (Simplified)
```
You are a helpful Islamic knowledge assistant.
Answer ONLY from the provided context below.
Do NOT use your own knowledge or training data.
Always cite the exact source URL at the end of your answer.
If the question is vague, ask a clarifying question.
If no relevant information is found, say:
"I could not find an answer in the approved Islamic sources.
 Please consult a qualified Islamic scholar."
```

---

## End-to-End Data Flow

```mermaid
flowchart LR
    A["🎤 User speaks\nTelugu/Urdu question"] --> B["📱 AudioRecord\n16kHz mono PCM"]
    B --> C{"Network\nTier?"}

    C -->|"Internet/LAN"| D["🖥️ faster-whisper\nGPU STT (~1s)"]
    C -->|"Offline"| E["📱 whisper.cpp\nJNI STT (~8s)"]

    D --> F["📝 Transcribed text\n(Telugu/Urdu)"]
    E --> F

    F --> G["🌐 IndicTrans2\nTranslate → English"]
    G --> H["🔍 Qdrant Search\n24,520 fatwas"]
    H --> I["🤖 LLM Generation\nStrict RAG prompt"]
    I --> J["📖 Cited answer\n+ Source URLs"]
    J --> K["🌐 IndicTrans2\nTranslate → Telugu/Urdu"]
    K --> L["📱 Display answer\nwith source links"]
    L --> M["🔊 TTS\nRead aloud"]
    L --> N["💾 Room DB\nSave to history"]
```

---

## Dynamic GPU Offloading

The RTX 3050 (4GB VRAM) cannot run all GPU models simultaneously. The backend implements **dynamic GPU offloading**:

```mermaid
sequenceDiagram
    participant CPU as CPU RAM
    participant GPU as GPU VRAM (4GB)
    participant SVC as Service

    Note over CPU,GPU: Models load to CPU by default

    SVC->>CPU: Model.to("cpu") at startup
    Note over GPU: GPU VRAM: 0 GB

    SVC->>GPU: Model.to("cuda") before inference
    Note over GPU: GPU VRAM: ~1.5 GB
    SVC->>SVC: Run inference
    SVC->>CPU: Model.to("cpu") after inference
    SVC->>SVC: gc.collect() + torch.cuda.empty_cache()
    Note over GPU: GPU VRAM: 0 GB (freed)
```

**Rule**: Only ONE GPU-heavy model runs at a time. Services are called sequentially (Translation → TTS → STT), never in parallel.

---

## Network Tier Detection

```kotlin
// Simplified NetworkUtils.kt logic
enum class NetworkTier { INTERNET, LAN, OFFLINE }

fun detectNetworkTier(context: Context): NetworkTier {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val network = cm.activeNetwork ?: return NetworkTier.OFFLINE
    val caps = cm.getNetworkCapabilities(network) ?: return NetworkTier.OFFLINE

    return if (pingServer("192.168.x.x", 8000, timeoutMs = 200)) {
        NetworkTier.LAN
    } else if (caps.hasCapability(NET_CAPABILITY_INTERNET)) {
        NetworkTier.INTERNET
    } else {
        NetworkTier.OFFLINE
    }
}
```

The app uses `ConnectivityManager.NetworkCallback` + `callbackFlow` for real-time reactive updates instead of polling.

---

## Technology Decision Matrix

| Decision | Chosen | Alternative Rejected | Why |
|----------|--------|---------------------|-----|
| Android UI | Jetpack Compose | XML layouts | Modern, declarative, Google's standard since 2021 |
| LLM | NVIDIA NIM + local Qwen3:4b | Cloud APIs (OpenAI, Anthropic) | Privacy + offline fallback |
| Vector DB | Qdrant (self-hosted) | Pinecone, Weaviate | Self-hosted, no data leaves infrastructure |
| RAG Framework | LlamaIndex | LangChain | Simpler API for our RAG use case |
| Translation | IndicTrans2 | Google Translate API | Privacy, self-hosted, best Indic language quality |
| TTS | Indic Parler TTS | Google TTS API | Self-hosted, natural Indian language voices |
| On-device STT | whisper.cpp (C++ JNI) | Google STT | Works fully offline, 99 languages |
| HTTP Client | Retrofit + OkHttp | Ktor, Volley | Most popular Android HTTP client, best documentation |
| DI Framework | Hilt | Dagger, Koin | Google recommended, works well with Compose |
| Local DB | Room + FTS5 | Firebase, Realm | Built-in Android, offline-first, full-text search |
| Backend Framework | FastAPI | Django, Flask | Fastest Python API, async, auto-generated docs |
| Orchestration | Docker Compose | Kubernetes, manual | Single command to start all services, portable |

---

> **Next**: See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for current build progress and known issues.
]]>
