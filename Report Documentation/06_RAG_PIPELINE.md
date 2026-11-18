# Report Documentation — Retrieval-Augmented Generation
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: BACKEND IMPLEMENTATION PRESENT; LIVE QDRANT AND ANSWER TESTS NOT VERIFIED

## What this layer does

Retrieval-Augmented Generation (RAG) searches the approved source collections, gives matching passages to a language model, and returns an answer with source URLs. It works like a research assistant: find source material first, then draft from that material.

## Current flow

```text
Android POST /query
  → translate question to English when needed
  → route question and expand to up to three search queries
  → embed with local Qwen3-Embedding-0.6B on CPU
  → search selected Qdrant collections
  → optionally rerank with CrossEncoder
  → generate answer using the configured LLM
  → return answer, source URLs, and expanded query
  → translate answer to requested language
```

## Current configuration

- Qdrant collection names: `islamqa` and `deoband`.
- Embeddings: local `qwen3_embedding_06b_local`; ingestion code configures 1,024 dimensions.
- Retrieval: custom multi-collection retriever; query routing, multi-query expansion, and reranking flags are accepted by the backend.
- Reranker: `cross-encoder/ms-marco-TinyBERT-L-2-v2` when enabled.
- LLM: NVIDIA NIM `openai/gpt-oss-20b` when `NVIDIA_API_KEY` is configured; otherwise local Ollama `qwen3:4b`.
- NIM is a cloud service. The question and retrieved context are sent to it when that provider is selected. Local Ollama is selected when the key is absent; the current code does not automatically retry with Ollama after a NIM request failure.
- The system prompt instructs the model to answer only from context and request source references. This is an instruction, not a code-level guarantee that every returned answer contains a valid citation.

## Files

- `backend/rag_pipeline.py` — retrieval, prompt, LLM choice, and response-source extraction.
- `backend/fastapi_server.py` — `/query` request schema, language handling, and response.
- `backend/ingest_islamqa.py` and `backend/ingest_deoband.py` — source collection population.
- `backend/.env.example` — placeholder environment variables; never put real keys in documentation.

## Verification and limits

The backend's live Qdrant state and query behavior were not testable during this audit because Qdrant was not running. Source SQLite counts are recorded in [07_KNOWLEDGE_SOURCES.md](./07_KNOWLEDGE_SOURCES.md), but they do not prove current collection counts.

The Android app has no local offline RAG/FTS search implementation. See [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md).

## See Also

- [04_TRANSLATION_PIPELINE.md](./04_TRANSLATION_PIPELINE.md) — language conversion
- [07_KNOWLEDGE_SOURCES.md](./07_KNOWLEDGE_SOURCES.md) — approved collections and ingestion
- [09_BACKEND_DOCKER.md](./09_BACKEND_DOCKER.md) — API services and ports
