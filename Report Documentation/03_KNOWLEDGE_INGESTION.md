# Report Documentation — Phase 3: Knowledge Ingestion
# Rushd-ul-Ilm (رشد العلم)

## Overview
Phase 3 focuses on transforming raw Islamic fatwa data into a format that the AI can understand and search semantically. This is the "brain" of our RAG (Retrieval-Augmented Generation) pipeline.

## 1. Vector Embeddings
We use vector embeddings to represent the meaning of fatwas as mathematical coordinates. This allows the app to find answers based on intent, even if the user uses different words than the source text.

**Model Used:** `paraphrase-multilingual-MiniLM-L12-v2` (384 dimensions)
- **Why:** Supports English, Telugu, Urdu, and 50+ other languages.
- **Why MiniLM:** It provides an excellent balance between accuracy and performance on CPU/low-VRAM hardware, and its smaller size (420MB) avoids download timeouts compared to the 1.1GB MPNET model.

## 2. Qdrant Vector Database
Qdrant stores our embeddings and their associated metadata (titles, URLs, snippets).

**Collection Details:**
- **Collections:** `islamqa` (Neutral) and `deoband` (Hanafi)
- **Vector Size:** 384
- **Distance Metric:** Cosine Similarity

## 3. Ingestion Process
The `backend/ingest_islamqa.py` and `backend/ingest_deoband.py` scripts perform the following:
1. Connect to the respective SQLite databases containing scraped fatwas.
2. Load the multilingual embedding model.
3. Iterate through fatwas in batches of 100.
4. Combine 'title' and 'question' (and 'answer' for Deoband) for semantic context.
5. Generate vectors and upsert them to Qdrant.

**Stats:**
- **Total Fatwas Ingested (IslamQA):** 15,739
- **Total Fatwas Ingested (Deoband):** 8,781
- **Ingestion Time:** ~2 minutes per source on developer hardware.

## 4. Multi-Source Search (MultiCollectionRetriever)
To support filtering by Islamic school of thought, the RAG pipeline (`backend/rag_pipeline.py`) implements a custom `MultiCollectionRetriever`.
- It receives a list of allowed sources from the Android app (e.g., `["islamqa", "deoband"]` or `["deoband"]`).
- It concurrently queries the requested Qdrant collections.
- It aggregates, sorts by similarity score, and feeds the top `k` most relevant nodes to the `ContextChatEngine` for answer generation.

## 5. Verification
Semantic search has been verified via internal testing.
- **Example Query:** "How to perform wudu?"
- **Top Result:** "Adhkaar al-Wudu (du`as to be recited when doing Wudu)"
- **Observation:** The system successfully identified the concept of Wudu without needing an exact keyword match.
