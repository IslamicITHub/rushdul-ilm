# MEM0_SETUP.md
# Rushd-ul-Ilm (رشد العلم) — Mem0 Self-Hosted Setup Guide
# Developer: Shaik Hidayatullah, Kurnool, Andhra Pradesh, India
# Last Updated: 2026-05-28
#
# ════════════════════════════════════════════════════════════════════
# WHAT IS MEM0?
# Mem0 (pronounced "mem-zero") is an open-source memory layer for AI agents.
# Think of it like a "memory" for your AI coding assistants.
#
# WITHOUT Mem0:
#   - Claude helps you build HomeScreen.kt → session ends
#   - New Claude session starts → it remembers NOTHING
#   - You have to explain the entire project again from scratch
#
# WITH Mem0:
#   - Claude builds HomeScreen.kt → automatically saves key facts to Mem0
#   - New Claude session starts → reads Mem0 → knows your name, the project,
#     last task completed, your preferences, and where to continue
#   - You say "continue the project" and it knows exactly what to do
#
# DIFFERENCE FROM GRAPHITI:
#   Mem0: Personal facts, preferences, session summaries (short, fast retrieval)
#   Graphiti: Full project knowledge graph with timestamps and relationships (rich, deep)
#   USE BOTH: They complement each other
#
# COST: 100% FREE — self-hosted using Docker + Ollama
# ════════════════════════════════════════════════════════════════════

---

## 🧠 What Mem0 Remembers (Examples)

```
"Developer prefers Kotlin explanations with Linux analogies"
"Developer is a beginner in Android Studio — always explain the IDE steps"
"Phase 1 is in progress. Last completed: NavGraph.kt"
"HomeScreen.kt is at android-app/app/src/main/java/com/rushdululilm/app/ui/screens/"
"Claude agent: explain Jetpack Compose before showing any Compose code"
"The developer's Ubuntu machine IP is 192.168.1.100"
"GTX 1650ti has 4GB VRAM — never suggest models above 3.5GB"
"Developer's primary OS is Parrot OS Linux — all bash commands should be for Linux"
```

---

## 🏗️ Architecture Overview

```
Your Ubuntu Machine
│
├── Qdrant (already running for Islamic Q&A on port 6333)
│   ^ Mem0 uses Qdrant to store memory embeddings
│   ^ No extra setup needed — same Qdrant instance
│
├── Ollama (already running on port 11434)
│   ^ Mem0 uses Ollama to extract key facts from conversations
│   ^ Uses qwen3:4b — same model as your Islamic Q&A
│   ^ NO API KEY NEEDED
│
├── Mem0 Server (new Docker service on port 8100)
│   ^ REST API that AI agents call to save/retrieve memories
│   ^ Runs as a Docker container
│
└── mem0_helper.py (your custom script)
    Functions: save_memory(), search_memory(), get_all_memories()
```

---

## ⚙️ STEP-BY-STEP INSTALLATION

### Option A: Self-Hosted Mem0 via Docker (Recommended)
**Note:** This option is not used and implemented and it will not be used in the future and instead, the python version will be used.
#### Step 1: Add Mem0 to Docker Compose

Add this to your `backend/docker-compose.yml`:

```yaml
# ─── Add this to your existing docker-compose.yml ───────────────────────────

  mem0:
    # Mem0 open-source server — self-hosted memory for AI agents
    image: mem0ai/mem0-server:latest
    # ^ Official Mem0 Docker image from Docker Hub

    container_name: rushd-mem0
    restart: unless-stopped

    ports:
      - "8100:8100"
      # ^ Port 8100 is where the Mem0 REST API will be available
      # ^ AI agents call http://localhost:8100 to save/retrieve memories

    environment:
      - MEM0_TELEMETRY=false
      # ^ Disable telemetry — no data sent to Mem0's servers

      - VECTOR_STORE_PROVIDER=qdrant
      # ^ Use our existing Qdrant container for storing memory vectors
      # ^ This means memories are searchable by meaning (semantic search)

      - QDRANT_HOST=qdrant
      # ^ 'qdrant' is the Docker service name of our Qdrant container
      # ^ Docker Compose automatically handles inter-container networking by service name

      - QDRANT_PORT=6333
      # ^ The port Qdrant listens on inside the Docker network

      - LLM_PROVIDER=ollama
      # ^ Use Ollama (local, free) instead of OpenAI to extract facts from text

      - OLLAMA_HOST=http://host.docker.internal:11434
      # ^ 'host.docker.internal' is a special Docker hostname that points to your Ubuntu machine
      # ^ This lets the Mem0 container reach Ollama which is running on your Ubuntu host

      - OLLAMA_MODEL=qwen3:4b
      # ^ Use Qwen3:4b for fact extraction — same model used for Islamic Q&A

      - EMBEDDER_PROVIDER=ollama
      # ^ Use Ollama for creating memory embeddings too

      - EMBEDDER_MODEL=nomic-embed-text
      # ^ Use the same embedding model we set up for Graphiti

    depends_on:
      - qdrant
      # ^ Wait for Qdrant to start before starting Mem0
      # ^ Mem0 needs Qdrant to store its memory vectors

    volumes:
      - mem0_data:/app/data
      # ^ Persistent storage for Mem0's configuration and SQLite metadata

volumes:
  # Add this to your existing volumes section
  mem0_data:
    # ^ Docker-managed storage for Mem0 data
```

#### Step 2: Start Mem0

```bash
# Start Mem0 (and Qdrant if not already running)
docker compose up -d qdrant mem0
# ^ Start both Qdrant and Mem0 in the background

# Check logs to confirm Mem0 started
docker compose logs mem0
# ^ Should see: "Mem0 server started on port 8100"

# Test that Mem0 API is working
curl http://localhost:8100/health
# ^ Should return: {"status": "ok"}
# ^ If you see this — Mem0 is running! ✅
```

### Option B: Mem0 Python Library (Simpler — No Extra Docker Container and currently this option is implemented)
**Note:** This option is already implemented. so no need to do it again
If you want to avoid another Docker container, use the Python library directly:

```bash
# Install Mem0 Python library in your knowledge-graph virtual environment
source /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/venv/bin/activate
pip install mem0ai
# ^ 'mem0ai' is the official Python package name for Mem0
```

---

### Step 3: Create the Mem0 Helper Script
**Note:** This step is already implemented.so no need to do it again
Here is the created file that uses Google Gemini's free tier API key, qdrant, and ollama's nomic-embed-text model: `knowledge-graph/mem0_helper.py`
---

## 🧪 TESTING MEM0
**Note:** This step is already implemented and performed .so no need to do it again
```bash
# Navigate to knowledge-graph folder
cd /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/

# Activate virtual environment
source venv/bin/activate

# Install mem0ai if not done yet
pip install mem0ai

# Test saving a session memory
python mem0_helper.py save "PROJECT SETUP COMPLETE. All rules files created. Phase 1 Android UI is next. Developer is beginner in Android Studio. Machine: Parrot OS Linux, GTX 1650ti 4GB VRAM."

# Test saving developer preferences
python mem0_helper.py save-preference "Always explain what a new Kotlin keyword does before using it in code"
python mem0_helper.py save-preference "Developer knows Python — use Python analogies to explain Kotlin concepts"
python mem0_helper.py save-preference "Developer is on Parrot OS Linux — all commands should use Linux syntax"

# Test searching
python mem0_helper.py search "current project status"
python mem0_helper.py search "developer preferences"

# List all memories
python mem0_helper.py list
```

---

## 🔄 HOW AI AGENTS USE MEM0

**At the START of every session:**
```bash
# Run these two commands in the knowledge-graph folder
python mem0_helper.py search "current phase next task"
python mem0_helper.py search "developer preferences"
```

**At the END of every session:**
```bash
python mem0_helper.py save "Phase [N]: [What was completed]. Files created: [list]. Next: [exact next task]."
```

---

## 🔑 USING FREE GOOGLE GEMINI API FOR MEM0 (This Faster Alternative is already implemented in the mem0_helper.py script to replace ollamas)

If Ollama-based extraction is slow, use Google Gemini's free tier:

```python
# In mem0_helper.py, replace the llm config section with:
"llm": {
    "provider": "gemini",
    "config": {
        "model": "gemini-2.5-flash",       # FREE model
        "api_key": "YOUR_FREE_KEY_FROM_AISTUDIO",
        # ^ Get free key at: https://aistudio.google.com/apikey
        # ^ Free tier: 15 requests/minute — more than enough for Mem0
    },
},
# Note: Islamic Q&A answers still use local Ollama
# Only Mem0's fact extraction uses Gemini — your user data stays private
```

---

## ❓ TROUBLESHOOTING

**Problem:** `Connection refused at localhost:8100`
**Cause:** Mem0 Docker container not running
**Fix:** `docker compose up -d mem0`

**Problem:** `Model qwen3:4b not found` in Mem0 logs
**Cause:** Ollama doesn't have the model downloaded
**Fix:** `ollama pull qwen3:4b`

**Problem:** Memories not found when searching
**Cause:** Different user_id used when saving vs searching
**Fix:** Always use the same DEVELOPER_ID constant — it's defined at the top of mem0_helper.py

**Problem:** Mem0 is slow at extracting memories
**Cause:** Qwen3:4b inference takes a few seconds per memory
**Fix:** This is normal. Mem0 processes memories in the background.
         Switch to Google Gemini free tier for faster extraction.

---

## 🔗 References

- Mem0 GitHub: https://github.com/mem0ai/mem0
- Mem0 Docs: https://docs.mem0.ai
- Mem0 Self-Hosted Guide: https://docs.mem0.ai/open-source/quickstart
- Ollama Models: https://ollama.com/library
- Google AI Studio (free API): https://aistudio.google.com/apikey
