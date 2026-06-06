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
- **Name:** `islamqa`
- **Vector Size:** 384
- **Distance Metric:** Cosine Similarity

## 3. Ingestion Process
The `backend/ingest_islamqa.py` script performs the following:
1. Connects to the SQLite database containing scraped fatwas.
2. Loads the multilingual embedding model.
3. Iterates through fatwas in batches of 100.
4. Combines 'title' and 'question' for semantic context.
5. Generates vectors and upserts them to Qdrant.

**Stats:**
- **Total Fatwas Ingested:** 15,739
- **Ingestion Time:** ~2 minutes on developer hardware.

## 4. Verification
Semantic search has been verified via internal testing.
- **Example Query:** "How to perform wudu?"
- **Top Result:** "Adhkaar al-Wudu (du`as to be recited when doing Wudu)"
- **Observation:** The system successfully identified the concept of Wudu without needing an exact keyword match.
