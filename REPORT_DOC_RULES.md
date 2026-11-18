# REPORT_DOC_RULES.md
# Rushd-ul-Ilm (رشد العلم) — Report Documentation Maintenance Rules
# Developer: Shaik Hidayatullah | Version 3.1
# Last Synced: 2026-11-18
#
# ════════════════════════════════════════════════════════════════════
# PURPOSE: This file tells every AI agent HOW to write and maintain
#          the Report Documentation/ folder. The docs in that folder
#          must be clear enough for:
#          1. A complete beginner in Android/Python to understand
#          2. Another AI agent (Claude, Gemini, Codex) to pick up and continue
#          3. The developer to review months later without confusion
# ════════════════════════════════════════════════════════════════════

---

## 📁 REPORT DOCUMENTATION FOLDER — FILE LIST

Every AI agent must maintain these 10 documentation files.
Update the relevant file(s) after every coding session.

| File | Covers | Update When |
|------|--------|-------------|
| `01_PROJECT_OVERVIEW.md` | Mission, audience, current scope, and implementation snapshot | Any major project or architecture change |
| `02_ANDROID_APP_LAYER.md` | Kotlin/Compose screens, ViewModels, repositories, Room saved-answer history | Android code changes |
| `03_STT_PIPELINE.md` | faster-whisper service and whisper.cpp JNI; document language and offline limits | STT changes |
| `04_TRANSLATION_PIPELINE.md` | IndicTrans2 service and the unimplemented Opus-MT offline target | Translation changes |
| `05_TTS_PIPELINE.md` | Indic Parler backend service and Android TextToSpeech fallback | TTS changes |
| `06_RAG_PIPELINE.md` | LlamaIndex/Qdrant retrieval, Qwen3 embeddings, NIM/Ollama selection, prompt constraints | Ingestion or answer-generation changes |
| `07_KNOWLEDGE_SOURCES.md` | Source SQLite databases, ingestion scripts, embedding configuration, verified counts | Source or ingestion changes |
| `08_VIDEO_DATABASE.md` | Current mock library and URL-metadata scraper; planned indexing/playback work | Video changes |
| `09_BACKEND_DOCKER.md` | Compose services, native Ollama, host mounts, FastAPI endpoints | Backend or deployment changes |
| `10_OFFLINE_STRATEGY.md` | Implemented offline paths and gaps in the intended three-tier design | Network or offline changes |

---

## 📝 HOW TO WRITE EACH DOCUMENTATION FILE

Use sections appropriate to the topic. Each report should make the current implementation and its limits clear, distinguish verified behavior from planned work, link to relevant source files, and include setup or test steps only when they have been verified.

Reports are not required to duplicate full source code. If code excerpts are necessary to explain behavior, keep them accurate, cite the source path, and label incomplete or unverified behavior.

---

## ✏️ WRITING STYLE RULES FOR DOCUMENTATION

### Rule W1 — Explain Every Technical Term
The first time a technical term appears in a file, explain it in brackets:
```
Example: "We use Qdrant (a vector database — think of it as a search engine that
          understands the MEANING of words, not just exact keywords) to find the
          most relevant fatwas for the user's question."
```

### Rule W2 — Use Analogies for Complex Concepts
Always include an analogy for complex concepts:
```
Example: "Hilt DI (Dependency Injection) works like a restaurant supply chain.
          Instead of each chef going to buy their own ingredients, the kitchen
          manager (Hilt) prepares and delivers everything each chef (ViewModel)
          needs before they even ask for it."
```

### Rule W3 — Show Before and After for Architecture Changes
When the architecture changes, show:
- What it was before
- Why it changed
- What it is now

### Rule W4 — Label All Code Clearly
Every code block must have:
- File path as a heading above it
- Language label on the code fence (kotlin, python, yaml, bash, etc.)
- A "What this does" paragraph before the code

### Rule W5 — Version Every Documentation Update
Every documentation file starts with:
```
# Last Updated: YYYY-MM-DD
# Updated by: [Agent Name]
# Changes: [brief description of what changed]
```

### Rule W6 — Cross-Reference Related Files
At the bottom of each file, list related documentation files:
```
## See Also
- 09_BACKEND_DOCKER.md — for how the FastAPI server is set up
- 10_OFFLINE_STRATEGY.md — for the offline fallback for this layer
```

---

## 🔄 DOCUMENTATION UPDATE CHECKLIST (Run After Every Session)

```
[ ] Updated "Last Updated" date at top of modified files
[ ] Added new code with full comments and explanations
[ ] Updated architecture diagram if structure changed
[ ] Added any new files to the "Files in This Layer" section
[ ] Added any new setup steps discovered during this session
[ ] Added any new common problems/fixes discovered
[ ] Cross-referenced any new connections to other layers
[ ] Appended session to ACTIVITY_LOG.md
```

---

## 🌟 QUALITY STANDARD FOR DOCUMENTATION

Ask yourself these questions before finishing documentation for a session:

1. **Beginner test**: Could a person who just installed Android Studio for the first time
   understand what this code does by reading the documentation and comments?

2. **Agent test**: Could a new AI agent (Claude, Gemini, Codex) read only this documentation
   file and the ACTIVITY_LOG.md and know exactly where to continue, what files exist,
   and what the code does — without asking the developer any questions?

3. **Completeness test**: Is every new file listed with its path and purpose?
   Is every new dependency listed with why it was chosen?

If any answer is "No" — improve the documentation before finishing the session.

---

*Rushd-ul-Ilm (رشد العلم) — Documentation Rules v3.0 | Shaik Hidayatullah, Kurnool, India*
