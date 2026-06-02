import os
# File: knowledge-graph/graphiti_config.py
# Purpose: Configure Graphiti to use local Ollama models instead of OpenAI
# This means: ZERO cost, ZERO data sent to cloud, works fully offline
# Developer: Shaik Hidayatullah

# ─── Import the Graphiti library and its configuration classes ───────────────
from graphiti_core import Graphiti
# ^ Graphiti is the main class — like the "brain" that manages the knowledge graph
from graphiti_core.llm_client.openai_generic_client import OpenAIGenericClient
from graphiti_core.llm_client.openai_client import OpenAIClient
# ^ Graphiti was built for OpenAI, but we can trick it to use Ollama
# ^ Ollama provides an OpenAI-compatible API at localhost:11434/v1
# ^ This means we use OpenAI's API format but point it to our LOCAL Ollama server

#from graphiti_core.embedder.openai_embedder import OpenAIEmbedder
from graphiti_core.embedder.openai import OpenAIEmbedder, OpenAIEmbedderConfig
# ^ Same trick for embeddings — use OpenAI format, point to Ollama

from graphiti_core.llm_client.config import LLMConfig
# ^ LLMConfig lets us set which model and which server to use
# Modified graphiti_config.py for Google Gemini (free tier)
from graphiti_core.llm_client.gemini_client import GeminiClient
# ^ Graphiti has a built-in Gemini client — no workarounds needed

GOOGLE_API_KEY = os.environ.get("GEMINI_API_KEY", "")
# ^ Replace with your key from https://aistudio.google.com/apikey


# ─── Ollama Configuration ─────────────────────────────────────────────────────
OLLAMA_BASE_URL = "http://localhost:11434/v1"
# ^ This is Ollama's OpenAI-compatible endpoint
# ^ Port 11434 is where Ollama runs on your Ubuntu machine
# ^ '/v1' is the API version path that Ollama exposes

OLLAMA_API_KEY = "ollama"
# ^ Ollama doesn't need a real API key, but the library requires SOMETHING here
# ^ The word "ollama" is just a placeholder — it doesn't matter what this says

OLLAMA_MODEL = "phi4-mini:3.8b"
# ^ Use Qwen3:4b — the same model used for Islamic Q&A
# ^ This model will extract facts from your project notes and session logs
# ^ It fits in 4GB VRAM (2.5GB actual usage)

EMBEDDING_MODEL = "nomic-embed-text"
# ^ nomic-embed-text is a free embedding model available via Ollama
# ^ Much smaller than the multilingual model — good for project notes
# ^ Install with: ollama pull nomic-embed-text

NEO4J_URI = "bolt://localhost:7687"
# ^ bolt:// is Neo4j's own protocol (faster than HTTP for database queries)
# ^ Port 7687 is the Neo4j Bolt port we exposed in docker-compose.yml

NEO4J_USER = "neo4j"
# ^ Default Neo4j username — must match what we set in docker-compose.yml

NEO4J_PASSWORD = "rushdululilm2026"
# ^ Must match the password we set in NEO4J_AUTH in docker-compose.yml


def create_graphiti_client():
    """
    Creates and returns a configured Graphiti client that uses:
    - Neo4j on localhost:7687 (graph storage)
    - Ollama on localhost:11434 (LLM for fact extraction — NO API KEY NEEDED)
    - nomic-embed-text via Ollama (embeddings — NO API KEY NEEDED)

    Returns:
        Graphiti: A ready-to-use Graphiti client object
    """

    # Configure the LLM (the AI model that will read text and extract facts)
    llm_config = LLMConfig(
        base_url=OLLAMA_BASE_URL,
        # ^ Tell Graphiti to use Ollama's API instead of OpenAI's
        model=OLLAMA_MODEL,
        # ^ Use Qwen3:4b model for fact extraction
        api_key=OLLAMA_API_KEY,
        # ^ Fake API key — Ollama doesn't check this
    )


    # Create the LLM client using OpenAI format but pointing to Ollama
    llm_client = OpenAIGenericClient(config=llm_config)
    #llm_client = GeminiClient(
    #config=LLMConfig(
    #    model="gemini-2.5-flash",  # FREE model
    #    api_key=GOOGLE_API_KEY,))
    # ^ OpenAIClient is just a wrapper that knows how to make API calls
    # ^ We configured it to call Ollama instead of OpenAI — it doesn't know the difference

    # Configure the embedding model (converts text to numbers for similarity search)
    embedder = OpenAIEmbedder(
        config=OpenAIEmbedderConfig(
            base_url=OLLAMA_BASE_URL,
            # ^ Same Ollama server, different model
            embedding_model=EMBEDDING_MODEL,
            # ^ Use nomic-embed-text for creating text embeddings
            api_key=OLLAMA_API_KEY,
            # ^ Same fake API key
        )
    )

    # Create and return the Graphiti client
    graphiti_client = Graphiti(
        uri=NEO4J_URI,
        # ^ Where to find the Neo4j graph database
        user=NEO4J_USER,
        # ^ Neo4j login username
        password=NEO4J_PASSWORD,
        # ^ Neo4j login password
        llm_client=llm_client,
        # ^ The AI model that will read and extract facts from our notes
        embedder=embedder,
        # ^ The model that converts text to numbers for similarity matching
    )

    return graphiti_client
    # ^ Return the ready-to-use client
