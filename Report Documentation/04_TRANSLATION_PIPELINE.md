# Report Documentation — Translation Pipeline
# Rushd-ul-Ilm (رشد العلم)
# Last Updated: 2026-11-18 | Updated by: Copilot
# Status: BACKEND SERVICE IMPLEMENTED; DEVICE PLACEMENT REQUIRES VERIFICATION

## What this layer does

The FastAPI gateway calls the IndicTrans2 service to translate a non-English question to English before RAG, then translates the answer back to the requested language. Think of it as a bilingual clerk between the questioner and the English-indexed search system.

## Current implementation

- `backend/translation_service.py` exposes `POST /translate` on port 8001.
- The service selects the English-to-Indic or Indic-to-English model based on the language tags.
- Translation is split by Markdown lines and table cells; bold and italic markers are temporarily represented as HTML tags.
- `backend/fastapi_server.py` calls the translator for the question and answer. Its helper returns the original text if the service call fails, so a response may be untranslated.
- Offline Opus-MT translation is not implemented in the Android app. ONNX Runtime is present as a Gradle dependency, but no translation inference/session code was found in the app source.

## Verification required before calling the service healthy

The current source sets `device = "cuda"`; model loading explicitly moves the model to CPU, while the translation path moves tokenized inputs to `device`. This conflicts with nearby comments that claim the service is CPU-only. Confirm the intended device placement and run a real `/translate` request before documenting a successful current runtime.

The old integration examples below this section were historical notes and do not establish that the current service has been tested with the current model files and runtime.

## Files

- `backend/translation_service.py` — model selection, Markdown processing, and `/translate`.
- `backend/fastapi_server.py` — language-code mapping and request/response translation.
- `backend/docker-compose.yml` — service definition and port 8001.
- `android-app/gradle/libs.versions.toml` — ONNX Runtime dependency declaration; not proof of an implemented offline translator.

## How to verify

Start the backend using the project's configured model directories and environment, call `/health`, then POST short plain text and Markdown to `/translate` in both directions. Check the returned text and service logs. No live verification was possible in this audit.

## See Also

- [03_STT_PIPELINE.md](./03_STT_PIPELINE.md) — speech input
- [06_RAG_PIPELINE.md](./06_RAG_PIPELINE.md) — translated query retrieval
- [10_OFFLINE_STRATEGY.md](./10_OFFLINE_STRATEGY.md) — offline translation status
