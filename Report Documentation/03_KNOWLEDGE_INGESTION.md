# Report Documentation — Phase 3: Knowledge Ingestion
# Rushd-ul-Ilm (رشد العلم)

## Overview
Phase 3 focuses on transforming raw Islamic fatwa data into a format that the AI can understand and search semantically. This is the "brain" of our RAG (Retrieval-Augmented Generation) pipeline.

## 1. Vector Embeddings
We use vector embeddings to represent the meaning of fatwas as mathematical coordinates. This allows the app to find answers based on intent, even if the user uses different words than the source text.

**Model Used:** `paraphrase-multilingual-MiniLM-L12-v2` (384 dimensions)
- **Why:** Supports English, Telugu, Urdu, and 50+ other languages.
- **Why MiniLM:** It provides an excellent balance between accuracy and performance on CPU/low-VRAM hardware, and its smaller size (420MB) avoids download timeouts.
- **Hardware Optimization:** Embeddings are generated on the **CPU** to save the 4GB GPU VRAM for LLM inference.

## 2. Qdrant Vector Database
Qdrant stores our embeddings and their associated metadata (titles, URLs, snippets).

**Collection Details:**
- **Collections:** `islamqa` (Neutral) and `deoband` (Hanafi)
- **Vector Size:** 384 (matching the MiniLM model)
- **Distance Metric:** Cosine Similarity

## 3. Ingestion Process
The `backend/ingest_islamqa.py` and `backend/ingest_deoband.py` scripts perform the following:
1. Connect to the respective SQLite databases containing scraped fatwas.
2. Load the multilingual embedding model.
3. Process fatwas in batches.
4. Combine metadata and text for semantic context.
5. Generate vectors and upsert them to Qdrant.

**Stats:**
- **Total Fatwas Ingested (IslamQA):** 15,739
- **Total Fatwas Ingested (Deoband):** 8,781

## 4. Advanced RAG Pipeline (backend/rag_pipeline.py)
The pipeline has been upgraded from a simple query engine to a sophisticated conversational agent.

### A. Multi-Source Search (MultiCollectionRetriever)
To support filtering by Islamic school of thought, the RAG pipeline implements a custom `MultiCollectionRetriever`.
- It concurrently queries requested Qdrant collections (`islamqa`, `deoband`).
- It aggregates results and sorts them by similarity score.

### B. Query Expansion
To improve retrieval accuracy, the system uses the LLM to rewrite the user's input into a detailed search query (e.g., adding synonyms like "Taharah" for "Wudu") before searching the vector database.

### C. Conversational "Ask-Back" Logic
The system uses a `ContextChatEngine` with a strict "Fiqh Specialist" system prompt:
- **Clarification:** If the user's question is too broad (e.g., just "Salah"), the AI is instructed to provide a brief overview and then ask for specific details (providing 10-15 examples of specific questions).
- **Strict Context:** The AI is strictly forbidden from using its own training data. It only answers from the retrieved fatwas.
- **Citations:** Every answer includes the exact source URLs and references to Hadith/Quran from the context.

### D. NVIDIA NIM Integration
The pipeline utilizes the **NVIDIA NIM API** (running the `meta/llama-3.1-70b-instruct` or similar high-capacity model) for ultra-fast inference while maintaining a local fallback to **Ollama (Qwen3:4b)** if the internet is unavailable.

## 5. Verification
End-to-end integration has been verified via the `/query` endpoint.
- **Specific Query:** Answered with high detail and tables.
- **Broad Topic:** Triggered the "ask-back" clarification logic.
- **Source Filtering:** Successfully limited results to specific collections (e.g., Deoband only).
- **History:** Context is maintained across multiple turns.
