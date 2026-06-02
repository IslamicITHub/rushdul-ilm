# REPORT_DOC_RULES.md
# Rushd-ul-Ilm (رشد العلم) — Report Documentation Maintenance Rules
# Developer: Shaik Hidayatullah | Version 3.0
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
| `01_PROJECT_OVERVIEW.md` | What the app does, who it's for, why each technology was chosen | Phase 0 setup, any major architectural decision |
| `02_ANDROID_APP_LAYER.md` | All Kotlin/Compose Android code — screens, ViewModels, Room DB | Phase 1 and Phase 4 |
| `03_STT_PIPELINE.md` | Speech-to-text — faster-whisper (online) and whisper.cpp JNI (offline) | Phase 5 |
| `04_TRANSLATION_PIPELINE.md` | IndicTrans2 (online) and Opus-MT ONNX (offline) | Phase 5 |
| `05_TTS_PIPELINE.md` | Coqui XTTS-v2 (online) and Android TextToSpeech (offline) | Phase 5 |
| `06_RAG_PIPELINE.md` | LlamaIndex + Qdrant + Qwen3:4b — how RAG works with cited answers | Phase 3 and Phase 4 |
| `07_KNOWLEDGE_SOURCES.md` | Scraping scripts, SQLite DB structure, offline DB download | Phase 3 |
| `08_VIDEO_DATABASE.md` | Video indexing, Whisper transcription, ExoPlayer integration | Phase 6 |
| `09_BACKEND_DOCKER.md` | docker-compose.yml, FastAPI endpoints, all Docker services | Phase 2 |
| `10_OFFLINE_STRATEGY.md` | Complete three-tier fallback system for all features | Phase 5 and Phase 6 |

---

## 📝 HOW TO WRITE EACH DOCUMENTATION FILE

Every documentation file MUST follow this structure exactly:

```markdown
# [File Name]
# Rushd-ul-Ilm — [Layer Name] Documentation
# Last Updated: YYYY-MM-DD by [Agent Name]
# Status: [NOT STARTED / IN PROGRESS / COMPLETE]

---

## 🎯 What This Layer Does (Plain English — 3-5 sentences)
[Explain what this component does in simple language.
 Use an analogy. Assume the reader knows nothing about Android/Python.]

---

## 🔗 How It Connects to Other Layers
[Show a simple ASCII diagram of how this component connects to others.
 Example:
   Android App (Kotlin)
       ↓ HTTP POST /transcribe
   FastAPI Server (Python, port 8000)
       ↓ calls
   faster-whisper (port 8003)
       ↓ returns
   Telugu text → back to Android App
]

---

## 📂 Files in This Layer
[List every file that belongs to this layer:
   - path/to/file.kt — what it does
   - path/to/another.py — what it does
]

---

## 🔧 Setup Instructions
[Step-by-step instructions to set up this component from scratch.
 Include exact terminal commands with comments.
 Example:
   # Install Android Studio on Parrot OS Linux
   sudo snap install android-studio --classic
   # ^ 'sudo' means "run as administrator" (like right-click Run as Admin in Windows)
   # ^ 'snap' is a package manager built into Ubuntu/Parrot OS
   # ^ '--classic' allows the app full system access (needed for Android Studio)
]

---

## 💻 Code — [Feature Name]

### What this code does (explain BEFORE showing code)
[2-4 sentences explaining what the code below does in plain English.
 Include analogy if helpful.]

### File: [exact file path]
```kotlin  ← (or python, yaml, bash as appropriate)
// Every line MUST have a comment
// Comment explains the WHY, not just the WHAT
```

---

## ✅ How to Test This Layer
[Exact steps to verify this layer is working correctly.
 Include expected outputs the developer should see.]

---

## 🐛 Common Problems and Fixes
[List any errors that commonly occur and their exact fix.
 Format:
   Problem: [error message or symptom]
   Cause: [why it happens]
   Fix: [exact commands or code changes to fix it]
]

---

## 🔗 References
[Links to official documentation for every technology used in this layer]
```

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
