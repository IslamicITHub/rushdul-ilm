# Report Documentation — Phase 3: Approved Knowledge Sources
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: INGESTION SCRIPTS PRESENT; LIVE QDRANT STATE NOT VERIFIED

## What this layer does

This layer keeps downloaded Islamic source records and converts them into vectors that the backend can search by meaning. The SQLite source databases are like the books in a library; the Qdrant collections are their searchable catalog.

## Approved sources and local source-database counts

The project currently ingests IslamQA.info and Darul Ifta Deoband. The source SQLite databases available during this audit contained 15,739 and 8,781 `fatwas` rows respectively. These are source-database counts, not a verified count of points currently stored in Qdrant; Qdrant was not running during the audit.

`islamqa.org` is listed as an approved source in project rules, but this repository has no corresponding ingestion script.

## Current embedding and collection configuration

- Both ingestion scripts point to the local `qwen3_embedding_06b_local` model.
- Both configure 1,024-dimensional cosine vectors.
- Collection names are `islamqa` and `deoband`.
- The IslamQA payload contains `id`, `url`, `source`, `question`, and `answer`.
- The Deoband payload contains `url`, `source`, and `question_and_answer`.
- The scripts recreate their target collections before inserting records. Confirm that replacing a collection is intended before rerunning either script against a populated Qdrant instance.

The earlier MiniLM / 384-dimension description was stale and has been removed.

## Files

- `backend/ingest_islamqa.py` — reads `islamqa_database.sqlite`, embeds question and answer text, and upserts records.
- `backend/ingest_deoband.py` — reads `offline.sqlite`, embeds the source text, and upserts records.
- `backend/download_qwen3_embed.py` — downloads the embedding model to the local model directory.
- `backend/rag_pipeline.py` — searches these collections; see [06_RAG_PIPELINE.md](./06_RAG_PIPELINE.md).
- `islamqa.info-offline/` and `darulifta-deoband.com-offline/` — local source-data folders, not Android Room databases.

## Verification and limitations

The source row counts were checked from the local SQLite databases in read-only mode on 2026-11-18. The Qdrant service was unavailable, so live collection counts, schema and search quality remain unverified. The ingestion scripts do not create an Android-downloadable knowledge database.

The Deoband source SQLite database contains FTS tables, but the Android Room database does not define an FTS table. Do not describe that source-side index as an on-device offline-search feature.

## See Also

- [06_RAG_PIPELINE.md](./06_RAG_PIPELINE.md) — retrieval and answer generation
- [09_BACKEND_DOCKER.md](./09_BACKEND_DOCKER.md) — backend services and ports
- [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md) — what currently works offline
