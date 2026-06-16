# GRAPHITI_SETUP.md
# Rushd-ul-Ilm (رشد العلم) — Graphiti Knowledge Graph Setup Guide
# Developer: Shaik Hidayatullah, Kurnool, Andhra Pradesh, India
# Last Updated: 2026-05-28
#
# ════════════════════════════════════════════════════════════════════
# WHAT IS GRAPHITI?
# Graphiti (github.com/getzep/graphiti) is an open-source Python library
# that builds a "knowledge graph" — think of it like a living map of your
# entire project. It remembers:
#   - What decisions were made (and WHEN)
#   - What files exist and how they connect to each other
#   - What each AI agent session completed
#   - What Islamic sources have been indexed
#   - What the next task is
#
# WHY DO WE NEED IT?
# When you switch from one AI agent to another (Claude → Gemini → Codex),
# the new agent normally has NO memory of your project. With Graphiti,
# any new agent can query the knowledge graph and instantly know
# the full project state — without you having to explain everything again.
#
# COST: 100% FREE — runs locally on your Ubuntu machine using Ollama
# ════════════════════════════════════════════════════════════════════

---

## 🧠 What is a Knowledge Graph? (Plain English)

A knowledge graph is like a mind map, but for your entire project.

**Regular database example:** "Video file X exists"
**Knowledge graph example:** "Video file X was transcribed on 2026-05-15 by faster-whisper,
the transcript has 1,247 tokens, it belongs to scholar Mufti Menk, and it was added to
Qdrant collection 'islamic_videos'. The decision to use faster-whisper instead of Google
Speech API was made on 2026-05-10 because of privacy requirements."

Graphiti stores not just WHAT things are, but HOW they relate and WHEN things happened.
This is called a "temporal knowledge graph" — temporal means time-aware.

---

## 🏗️ Architecture Overview

```
Your Ubuntu Machine (Parrot OS / Ubuntu)
│
├── Neo4j (Graph Database) — stores the actual knowledge graph
│   Port: 7474 (web browser UI), Port: 7687 (app connection)
│   Docker container: neo4j:5-community (FREE version)
│
├── Graphiti (Python library) — reads/writes to Neo4j
│   Installed via: pip install graphiti-core
│   Your Python scripts import and use Graphiti
│
├── Ollama (Local LLM runtime)
│   Port: 11434 (already running for your main app)
│   Model: qwen3:4b (same model used for Islamic Q&A)
│   Graphiti uses Ollama to extract facts from text (no API key needed)
│
└── graphiti_helper.py (Your custom script — created in this guide)
    Functions: save_session(), search_project(), add_decision(), etc.
```

---

## ⚙️ STEP-BY-STEP INSTALLATION

### Step 1: Add Neo4j to Your Docker Compose
**Note:** This step is already implemented and performed .so no need to do it again
Open your `backend/docker-compose.yml` file and add this service block:

```yaml
services:
  neo4j:
    # Neo4j is the graph database that stores all Graphiti knowledge
    # neo4j:5-community is the FREE community edition — no license needed
    image: neo4j:5-community

    container_name: rushd-neo4j
    # ^ Give it a memorable name so we can reference it easily

    restart: unless-stopped
    # ^ Auto-restart if it crashes, but stop when you manually run 'docker compose down'

    ports:
      - "7474:7474"
      # ^ Port 7474 is the Neo4j web browser UI — open http://localhost:7474 to see your graph
      - "7687:7687"
      # ^ Port 7687 is the Bolt protocol port — Graphiti Python library connects here

    environment:
      - NEO4J_AUTH=neo4j/rushdululilm2026
      # ^ Sets username=neo4j, password=rushdululilm2026
      # ^ CHANGE THIS PASSWORD if you ever expose this server to the internet
      - NEO4J_PLUGINS=["apoc"]
      # ^ APOC is a plugin that adds extra features to Neo4j (needed by Graphiti)
      - NEO4J_dbms_security_procedures_unrestricted=apoc.*
      # ^ Allows APOC procedures to run — required for Graphiti to work correctly

    volumes:
      - neo4j_data:/data
      # ^ Stores the graph database files permanently (survives container restarts)
      - neo4j_logs:/logs
      # ^ Stores Neo4j logs for debugging

volumes:
  # Add these to your existing volumes section (or create the section if it doesn't exist)
  neo4j_data:
    # ^ Docker will create and manage this storage volume automatically
  neo4j_logs:
    # ^ Docker will create and manage this storage volume automatically
```

### Step 2: Start Neo4j
**Note:** This step is already implemented and performed .so no need to do it again
```bash
# Navigate to your backend folder
cd /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/backend/

# Start ALL services including the new Neo4j container
# The -d flag means "detached" — runs in background, not blocking your terminal
docker compose up -d neo4j

# Check that Neo4j started successfully
docker compose logs neo4j
# ^ You should see: "Started." in the logs — this means Neo4j is running

# Wait about 30 seconds for Neo4j to fully start, then open the web UI
# Open your browser and go to: http://localhost:7474
# Username: neo4j
# Password: rushdululilm2026
# If you see a graph database UI — Neo4j is working! ✅
```

### Step 3: Install Graphiti Python Library
**Note:** This step is already implemented and performed .so no need to do it again
```bash
# Make sure you are NOT inside a Docker container — run this on your Ubuntu host machine
# (Open a normal terminal in Parrot OS)

# Create a Python virtual environment specifically for Graphiti
# A virtual environment is like a clean isolated box for Python packages
# so they don't interfere with other Python projects on your machine
python3 -m venv /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/venv
# ^ python3 -m venv → use Python's built-in venv (virtual environment) tool
# ^ The path at the end is where the virtual environment folder will be created

# Activate the virtual environment
# IMPORTANT: You must activate it every time you open a new terminal
source /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/venv/bin/activate
# ^ 'source' runs the activate script, which sets up your terminal to use this Python
# ^ After running this, your terminal prompt will show (venv) at the start

# Install Graphiti and its required packages
pip install graphiti-core
# ^ graphiti-core is the main Graphiti library from getzep/graphiti on GitHub

pip install neo4j
# ^ Python driver for connecting to Neo4j database

pip install httpx
# ^ HTTP library needed by Graphiti for some operations

# Verify installation
pip show graphiti-core
# ^ Should show: Name: graphiti-core, Version: [version number]
```

### Step 4: Configure Graphiti to Use Ollama (FREE — No API Key)
**Note:** This step is already implemented and performed .so no need to do it again
Here is the created file that uses Google Gemini's free tier API key, and ollama's nomic-embed-text model: `knowledge-graph/graphiti_config.py`



### Step 5: Install Ollama Embedding Model
**Note:** This step is already implemented and performed .so no need to do it again
```bash
# Pull the nomic-embed-text embedding model (it's free and small — ~270MB)
# Make sure Ollama is running first
ollama pull nomic-embed-text
# ^ 'ollama pull' downloads a model — like 'pip install' but for AI models

# Verify it downloaded
ollama list
# ^ Should show: nomic-embed-text in the list alongside qwen3:4b
```

### Step 6: Create the Graphiti Helper Script

Here is the created file that uses Google Gemini's free tier API key, and ollama's nomic-embed-text model: `knowledge-graph/graphiti_helper.py`


---

## 🧪 TESTING GRAPHITI
**Note:** This step is already implemented and performed .so no need to do it again
After setup, test everything with these commands:

```bash
# Navigate to the knowledge-graph folder
cd /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/

# Activate the virtual environment
source venv/bin/activate
# ^ Your prompt should show (venv) now

# First: initialize Graphiti's required indexes in Neo4j
export $GEMINI_API_KEY=AQ.Ab8RN6KImltPQFfFLL2kg2arCOthkDWzPGP2nd3Nm-cZpwi96Q
export $OPENAI_API_KEY=ollama

python3 -c "
import asyncio
from graphiti_config import create_graphiti_client
async def init():
    client = create_graphiti_client()
    await client.build_indices_and_constraints()
    # ^ This creates the required database structure in Neo4j (only needed once)
    print('✅ Graphiti indexes created successfully')
    await client.close()
asyncio.run(init())
"

# Test saving a session
python graphiti_helper.py save-session "SETUP SESSION: Installed Graphiti and Neo4j. Configured Ollama. Ready to begin Phase 1 Android UI."

# Test searching (should return what we just saved)
python graphiti_helper.py search "current project status"

# Test adding a decision
python graphiti_helper.py add-decision "Use Neo4j Community Edition" "Free, open-source, fully self-hosted, no data sent to cloud, exactly fits our privacy requirements"
```

---

## 🔗 HOW AI AGENTS USE GRAPHITI

At the **START** of every coding session, the agent runs:
```bash
cd /home/hidayat/Documents/Islamic\ Knowledge\ Q\&A\ App/knowledge-graph/
source venv/bin/activate
export $GEMINI_API_KEY=your_gemini_api_key
export $OPENAI_API_KEY=ollama
python graphiti_helper.py search "current phase next task"
python graphiti_helper.py search "files created last session"
```

At the **END** of every coding session, the agent runs:
```bash
python graphiti_helper.py save-session "Phase [N]: Created [files]. [Description of what was built]. Next task: [specific next step]."
```

---

## 🔍 Viewing the Knowledge Graph (Visual)
**Note:** This step is already implemented and performed .so no need to do it again
1. Open your browser: http://localhost:7474
2. Login: neo4j / rushdululilm2026
3. Run this Cypher query to see all project nodes:
   ```cypher
   MATCH (n) RETURN n LIMIT 50
   ```
4. You'll see a visual map of all saved facts and their connections.

---

## 🌐 Using Google's Free APIs Instead of Ollama (This one is already implemented in the graphiti_config.py script to use gemini instead of ollama) 
**Note:** This step is already implemented and performed .so no need to do it again
If Ollama is too slow on your machine for Graphiti's fact extraction, you can use
Google Gemini's FREE API tier instead:

```bash
# Get your FREE Google Gemini API key from:
# https://aistudio.google.com/apikey
# (No credit card needed for the free tier)

# The free tier as of 2026 includes:
# - Gemini 1.5 Flash: 15 requests per minute, 1 million tokens per minute (FREE)
# - More than enough for Graphiti's background fact extraction

pip install google-generativeai
# ^ Install Google's Python library
```

```python
# Modified graphiti_config.py for Google Gemini (free tier)

from graphiti_core.llm_client.gemini_client import GeminiClient
# ^ Graphiti has a built-in Gemini client — no workarounds needed

GOOGLE_API_KEY = "YOUR_FREE_API_KEY_FROM_AISTUDIO"
# ^ Replace with your key from https://aistudio.google.com/apikey

llm_client = GeminiClient(
    config=LLMConfig(
        model="gemini-1.5-flash",  # FREE model
        api_key=GOOGLE_API_KEY,
    )
)
# ^ Use this llm_client instead of the Ollama-based one in graphiti_config.py
# ^ Everything else in graphiti_config.py stays the same
# ^ Note: Islamic Q&A answers still use local Ollama — only Graphiti uses Google
# ^ Your Islamic Q&A data never goes to Google — only project metadata does
```

---

## ❓ TROUBLESHOOTING

**Problem:** `Connection refused` when running graphiti_helper.py
**Cause:** Neo4j container is not running
**Fix:**
```bash
docker compose up -d neo4j
docker compose logs neo4j  # Wait for "Started." message
```

**Problem:** `Model not found: nomic-embed-text`
**Cause:** Embedding model not downloaded
**Fix:**
```bash
ollama pull nomic-embed-text
```

**Problem:** Graphiti seems slow at extracting facts
**Cause:** Qwen3:4b is doing LLM inference — takes a few seconds per episode
**Fix:** This is normal. Graphiti runs in the background — it won't block your coding.
         Alternatively, switch to Google Gemini free tier (faster).

---

## 🔗 References

- Graphiti GitHub: https://github.com/getzep/graphiti
- Graphiti Docs: https://help.getzep.com/graphiti
- Neo4j Community: https://neo4j.com/download/
- Ollama Models: https://ollama.com/library
- Google AI Studio (free API): https://aistudio.google.com/apikey
