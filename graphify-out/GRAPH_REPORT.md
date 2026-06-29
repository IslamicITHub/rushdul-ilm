# Graph Report - ./  (2026-06-29)

## Corpus Check
- 96 files · ~100,322 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 310 nodes · 457 edges · 55 communities (32 shown, 23 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.79)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Backend Services|Backend Services]]
- [[_COMMUNITY_Settings & Preferences|Settings & Preferences]]
- [[_COMMUNITY_App Core Logic|App Core Logic]]
- [[_COMMUNITY_Knowledge RAG Pipeline|Knowledge RAG Pipeline]]
- [[_COMMUNITY_Local Database|Local Database]]
- [[_COMMUNITY_Network & API|Network & API]]
- [[_COMMUNITY_UI & Theming|UI & Theming]]
- [[_COMMUNITY_Audio & Repository|Audio & Repository]]
- [[_COMMUNITY_Navigation Screens|Navigation Screens]]
- [[_COMMUNITY_Home ViewModel|Home ViewModel]]
- [[_COMMUNITY_Whisper JNI C++|Whisper JNI C++]]
- [[_COMMUNITY_Android Application Base|Android Application Base]]
- [[_COMMUNITY_App Architecture Docs|App Architecture Docs]]
- [[_COMMUNITY_FastAPI Requirements|FastAPI Requirements]]
- [[_COMMUNITY_Translation Requirements|Translation Requirements]]
- [[_COMMUNITY_LlamaIndex Requirements|LlamaIndex Requirements]]
- [[_COMMUNITY_Qdrant Requirements|Qdrant Requirements]]
- [[_COMMUNITY_STT Requirements|STT Requirements]]
- [[_COMMUNITY_Offline Fallback Rules|Offline Fallback Rules]]
- [[_COMMUNITY_AI Prompt Rules|AI Prompt Rules]]
- [[_COMMUNITY_Android App Build|Android App Build]]
- [[_COMMUNITY_Android Settings|Android Settings]]
- [[_COMMUNITY_Namaz Audio MP3|Namaz Audio MP3]]
- [[_COMMUNITY_Namaz Audio WAV|Namaz Audio WAV]]
- [[_COMMUNITY_Sentence Transformers Req|Sentence Transformers Req]]
- [[_COMMUNITY_Wazu Audio MP3|Wazu Audio MP3]]
- [[_COMMUNITY_Wazu Audio WAV|Wazu Audio WAV]]
- [[_COMMUNITY_Whisper JNI Audio|Whisper JNI Audio]]
- [[_COMMUNITY_Android Docs|Android Docs]]
- [[_COMMUNITY_Video Docs|Video Docs]]
- [[_COMMUNITY_Graphiti Design|Graphiti Design]]
- [[_COMMUNITY_Mem0 Design|Mem0 Design]]
- [[_COMMUNITY_Sprint Microtasks|Sprint Microtasks]]
- [[_COMMUNITY_Offline Orange Color|Offline Orange Color]]

## God Nodes (most connected - your core abstractions)
1. `AnswerViewModel` - 17 edges
2. `HomeViewModel` - 16 edges
3. `RushdulIlmTheme()` - 14 edges
4. `SavedAnswer` - 13 edges
5. `SettingsViewModel` - 12 edges
6. `SavedAnswerDao` - 10 edges
7. `ApiService` - 10 edges
8. `RushdulIlmDatabase` - 9 edges
9. `MainRepository` - 9 edges
10. `UserPreferencesRepository` - 8 edges

## Surprising Connections (you probably didn't know these)
- `HomeScreen()` --calls--> `LanguageSelector()`  [INFERRED]
  android-app/app/src/main/java/com/rushdululilm/app/ui/screens/HomeScreen.kt → android-app/app/src/main/java/com/rushdululilm/app/ui/components/LanguageSelector.kt
- `HomeScreen()` --calls--> `MicButton()`  [INFERRED]
  android-app/app/src/main/java/com/rushdululilm/app/ui/screens/HomeScreen.kt → android-app/app/src/main/java/com/rushdululilm/app/ui/components/MicButton.kt
- `HomeScreen()` --calls--> `SourceSelector()`  [INFERRED]
  android-app/app/src/main/java/com/rushdululilm/app/ui/screens/HomeScreen.kt → android-app/app/src/main/java/com/rushdululilm/app/ui/components/SourceSelector.kt
- `RushdulIlmNavGraph()` --calls--> `SettingsScreen()`  [INFERRED]
  android-app/app/src/main/java/com/rushdululilm/app/ui/screens/NavGraph.kt → android-app/app/src/main/java/com/rushdululilm/app/ui/screens/SettingsScreen.kt
- `lifespan()` --calls--> `RagPipeline`  [INFERRED]
  backend/fastapi_server.py → backend/rag_pipeline.py

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **room_database_system** — local_rushdulilmdatabase_rushdulilmdatabase, local_savedanswerdao_savedanswerdao, local_savedanswer_savedanswer, local_fatwasourceconverter_fatwasourceconverter [INFERRED 0.85]
- **network_communication_layer** — remote_apiservice_apiservice, remote_networkmodels_queryrequest, remote_networkmodels_queryresponse, di_networkmodule_networkmodule [INFERRED 0.85]
- **android_viewmodels** — viewmodel_answershistoryviewmodel, viewmodel_homeviewmodel, viewmodel_settingsviewmodel, viewmodel_videolibraryviewmodel [INFERRED 0.85]
- **backend_fastapi_services** — backend_fastapi_server, backend_stt_service, backend_translation_service, backend_tts_service [INFERRED 0.85]
- **backend_model_downloaders** — backend_download_indictrans2, backend_download_model, backend_download_qwen3_embed, backend_download_tts [INFERRED 0.85]
- **docker_compose_services** — backend_fastapi_service, backend_qdrant_service, backend_indictrans2_service, backend_tts_service, backend_stt_service [INFERRED 0.95]

## Communities (55 total, 23 thin omitted)

### Community 0 - "Backend Services"
Cohesion: 0.09
Nodes (26): ask_question(), lifespan(), QueryRequest, Translates text by calling our local IndicTrans2 translation service on port 800, translate_text(), FastAPI Service, IndicTrans2 Service, Qdrant Service (+18 more)

### Community 1 - "Settings & Preferences"
Cohesion: 0.10
Nodes (13): Boolean, LanguageSelector(), FloatArray, AppLanguage, UserPreferencesRepository, DownloadItem(), SettingsRadioButton(), SettingsScreen() (+5 more)

### Community 2 - "App Core Logic"
Cohesion: 0.11
Nodes (17): Context, List, FatwaSourceConverter, MediaPlayer, FatwaAnswer, FatwaSource, RelatedVideo, StateFlow (+9 more)

### Community 3 - "Knowledge RAG Pipeline"
Cohesion: 0.10
Nodes (15): ingest(), test_search(), ingest(), ingest(), # File: backend/ingest_islamqa.py # Purpose: Converts IslamQA SQLite fatwas into, Takes a plain text query, converts it to an embedding, and asks Qdrant      to f, Reads data from SQLite, converts text to embeddings, and uploads to Qdrant., test_qdrant_search() (+7 more)

### Community 4 - "Local Database"
Cohesion: 0.14
Nodes (8): DatabaseModule, Flow, Int, RushdulIlmDatabase, SavedAnswer, SavedAnswerDao, AnswerHistoryRepository, RoomDatabase

### Community 5 - "Network & API"
Cohesion: 0.12
Nodes (14): NetworkModule, Map, okhttp3, OkHttpClient, ApiService, ChatMessage, QueryRequest, QueryResponse (+6 more)

### Community 6 - "UI & Theming"
Cohesion: 0.12
Nodes (15): MainActivity, AppCompatActivity, Bundle, MicButton(), MicButtonPreviewNotRecording(), MicButtonPreviewRecording(), SourceSelector(), SourceSelectorPreview() (+7 more)

### Community 7 - "Audio & Repository"
Cohesion: 0.14
Nodes (12): AudioRecord, com, java, QueryRequest, MainRepository, T, TTSResponse, AudioRecorderHelper (+4 more)

### Community 8 - "Navigation Screens"
Cohesion: 0.15
Nodes (10): VideoCard(), NavController, AnswerScreen(), AnswerVideoCard(), AnswersHistoryScreen(), SavedAnswerCard(), HomeScreen(), RushdulIlmNavGraph() (+2 more)

### Community 9 - "Home ViewModel"
Cohesion: 0.24
Nodes (7): Error, HomeUiState, HomeViewModel, Idle, NavigatingToAnswer, Processing, Recording

### Community 10 - "Whisper JNI C++"
Cohesion: 0.40
Nodes (9): Java_com_rushdululilm_app_utils_WhisperHelper_freeModel(), Java_com_rushdululilm_app_utils_WhisperHelper_getSystemInfo(), Java_com_rushdululilm_app_utils_WhisperHelper_initModel(), Java_com_rushdululilm_app_utils_WhisperHelper_transcribeAudio(), jfloatArray, JNIEnv, JNIEXPORT, jobject (+1 more)

## Knowledge Gaps
- **3 isolated node(s):** `ChatMessage`, `TTSRequest`, `TTSResponse`
  These have ≤1 connection - possible missing edges or undocumented components.
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `HomeViewModel` connect `Home ViewModel` to `Navigation Screens`, `Settings & Preferences`, `App Core Logic`, `UI & Theming`?**
  _High betweenness centrality (0.058) - this node is a cross-community bridge._
- **Why does `MainRepository` connect `Audio & Repository` to `App Core Logic`, `Local Database`, `Network & API`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Why does `RushdulIlmTheme()` connect `UI & Theming` to `Settings & Preferences`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `RushdulIlmTheme()` (e.g. with `.onCreate()` and `MicButtonPreviewNotRecording()`) actually correct?**
  _`RushdulIlmTheme()` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ChatMessage`, `TTSRequest`, `TTSResponse` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Backend Services` be split into smaller, more focused modules?**
  _Cohesion score 0.09090909090909091 - nodes in this community are weakly interconnected._
- **Should `Settings & Preferences` be split into smaller, more focused modules?**
  _Cohesion score 0.10416666666666667 - nodes in this community are weakly interconnected._