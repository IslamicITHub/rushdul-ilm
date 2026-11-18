# Report Documentation — Phase 2: Backend Docker Services

This document tracks the progress and implementation details of the backend services for the Rushd-ul-Ilm project.

> **Current implementation snapshot (2026-11-18):** This file contains older sprint notes; treat their historical status and verification claims as history, not current runtime state. The current Compose file defines FastAPI (host networking), Qdrant, IndicTrans2, Parler TTS, and faster-whisper. Ollama is expected to run natively on the host, not as an active Compose service. The FastAPI, translation, and TTS images use Python 3.13; the STT image uses Python 3.11. Services mount host virtual environments and local model folders, so this is not a clean, self-contained Docker build. FastAPI creates `RagPipeline` in its lifespan; TTS loads its model at service startup and moves it to CUDA for requests; STT loads and releases its model per transcription request. Translation source loads models into CPU memory, moves the model and inputs to CUDA for inference, then moves the model back to CPU; this path has not been runtime-verified and has no CPU fallback because `device` is hard-coded to `"cuda"`. Compose runs one Uvicorn worker per service without `--reload`; GPU resource handling remains service-specific, with no global concurrency guarantee.

The live Qdrant service was not running during the documentation audit. The current LLM selection is NVIDIA NIM `openai/gpt-oss-20b` when `NVIDIA_API_KEY` is configured, otherwise local Ollama `qwen3:4b`. See [06_RAG_PIPELINE.md](./06_RAG_PIPELINE.md) and [SECURITY.md](../SECURITY.md) for the cloud-data boundary.

## 🚀 Phase 2 Overview
**Goal:** All local AI services (Ollama, Qdrant, FastAPI) running in Docker containers with GPU acceleration enabled.

> **Historical plan:** The original goal above is retained as a sprint record. It does not describe the current deployment: Ollama runs natively on the host, while the other listed services are defined in Compose.

---

## 🛠️ Sprint 2.1 — Install Docker & NVIDIA Toolkit
**Status:** ✅ COMPLETE

### 1. Docker Engine Installation
Docker was verified to be already installed and running on the host system.
- **Docker version:** 29.5.2
- **Docker Compose version:** v2.40.3-desktop.1
- **User Permissions:** User `hidayat` was confirmed to be in the `docker` group, allowing for rootless container management.

**Verification:**
The `hello-world` container was successfully pulled and executed.

### 2. NVIDIA Container Toolkit
The NVIDIA Container Toolkit was installed to allow Docker containers to access the host's GPU (NVIDIA RTX 3050).

**Installation Steps:**
1. Added NVIDIA's package repository GPG key and repository list.
2. Installed `nvidia-container-toolkit`.
3. Configured the Docker daemon to use the NVIDIA runtime:
   ```bash
   sudo nvidia-ctk runtime configure --runtime=docker
   ```
4. Restarted the Docker service.

**Verification:**
A CUDA-enabled container (`nvidia/cuda:12.4.1-base-ubuntu22.04`) was run to execute `nvidia-smi`.
- **Result:** Success. The container correctly detected the RTX 3050 GPU and its 4GB of VRAM.

---

## 🛠️ Sprint 2.2 — Create docker-compose.yml
**Status:** ✅ COMPLETE

### 1. Base docker-compose.yml Created
The base orchestration file was created in `backend/docker-compose.yml`.

**Key Configurations:**
- **Custom Network:** `rushd-network` (bridge driver) was created to allow isolated communication between AI services using internal DNS.
- **Persistent Volumes:**
  - `ollama_data`: To cache downloaded LLM models and prevent re-downloading large files.
  - `qdrant_data`: To persist the embedded Islamic knowledge base between restarts.
- **Service Placeholders:** Extensive, beginner-friendly comments were added acting as placeholders for the upcoming Sprint tasks (Ollama, FastAPI, Qdrant, etc.). A minimal placeholder service (`setup_placeholder`) was added to ensure the file validates against the Docker Compose syntax checker.

**Verification:**
The syntax was verified with `docker compose -f backend/docker-compose.yml config`.

---

## 🛠️ Sprint 2.3 — Ollama + Qwen3:4b Setup
**Status:** ✅ COMPLETE (Logically)

### 1. Ollama Configured in Docker Compose
The `ollama` service was successfully added to the `docker-compose.yml` file.

**Key Configurations:**
- **Image:** `ollama/ollama:latest`
- **GPU Pass-through:** Configured using the `deploy.resources` block mapped to the NVIDIA driver, ensuring the RTX 3050 handles the LLM inference.
- **Port Mapping:** Port 11434 mapped from container to host.
- **Model Storage:** The host directory `~/.ollama` was bind-mounted to `/root/.ollama` within the container.

**Accommodation for Native Server:**
The developer indicated they are already running `ollama serve` natively and have pulled `qwen3:4b`.
- By bind-mounting the host directory, the Docker container instantly has access to the already-downloaded 2.5GB model.
- We deliberately skipped running `docker compose up -d ollama` during this sprint to prevent a port collision (11434 is in use by the native process).

---

## 🛠️ Sprint 2.4 — FastAPI Server Skeleton
**Status:** 🟡 IN PROGRESS

### 1. Create FastAPI App & Requirements
The foundational Python files for the backend API were created.
- **`backend/requirements.txt`**: Defined dependencies (`fastapi`, `uvicorn`, `requests`, `pydantic`) required to run the server.
- **`backend/fastapi_server.py`**: Created the main FastAPI application instance with a basic `@app.get("/health")` endpoint to serve as a liveness probe. Both files were thoroughly commented to ensure beginner accessibility.

### 2. Containerize FastAPI Service
A `Dockerfile` was created to package the Python API. It utilizes the lightweight `python:3.11-slim` base image and handles dependency installation. 

The `fastapi` service was then added to `docker-compose.yml`:
- **Build Context:** Configured to build from the local `./backend` directory.
- **Port:** 8000 mapped to the host.
- **Historical development setup:** The backend directory was bind-mounted to `/app`; an earlier command used Uvicorn's `--reload` flag.
- **Current setup:** Compose still bind-mounts the code, but starts Uvicorn with one worker and without `--reload`. The current FastAPI lifespan initializes `RagPipeline` at startup and clears its reference at shutdown.

**Verification:**
The container was built and started. A `curl` request to `http://localhost:8000/health` successfully returned the expected JSON payload, confirming the API is reachable.

## 🛠️ Sprint 2.5 — Qdrant Vector DB Setup
**Status:** ✅ COMPLETE

### 1. Configure Qdrant Service
The Qdrant vector database was integrated into the Docker Compose setup.
- **Image:** `qdrant/qdrant:latest`
- **Ports:** Mapped 6333 (API / Dashboard) and 6334 (gRPC) to the host.
- **Data Persistence:** Mounted the named volume `qdrant_data` to `/qdrant/storage` to ensure that vector embeddings for the Islamic knowledge base survive container restarts.

**Verification:**
The container was started, and the API root at `http://localhost:6333/` was queried, successfully returning the Qdrant version and title, confirming the service is active.

---

## 🛠️ Sprint 2.6 — Wire FastAPI to Ollama
**Status:** ✅ COMPLETE

### 1. Implement /query Endpoint
The `/query` endpoint was added to `fastapi_server.py` to bridge the gap between the Android app and the local LLM.

**Technical Challenges & Resolution:**
- **Host Connectivity:** Initially, the FastAPI container (in Docker) could not reach the native Ollama service (on the host) because the native service was bound exclusively to `127.0.0.1`.
- **Docker Network Mode:** To resolve this without forcing host-level configuration changes, the `fastapi` service in `docker-compose.yml` was switched to `network_mode: host`. This allows the container to share the host's IP stack and reach `localhost:11434` directly.
- **Internal Service:** The internal `ollama` service definition was commented out in `docker-compose.yml` to prevent failing image pulls and save disk space.

**Implementation Details:**
- **Pydantic Model:** Created `QueryRequest` to validate incoming JSON (e.g., `{"question": "..."}`).
- **Requests Library:** Used the `requests` library to forward the question to Ollama's `api/generate` endpoint.
- **Response Handling:** Extracts the generated response and returns it to the client as `{"answer": "..."}`.

**Verification:**
A `curl` request was sent to the backend:
```bash
curl -X POST http://localhost:8000/query \
     -H "Content-Type: application/json" \
     -d '{"question": "What is the importance of Salah in Islam?"}'
```
- **Result:** Success. The server returned a comprehensive, structured answer generated by the `qwen3:4b` model.

---

## 🛠️ Sprint 2.7 — Phase 2 Integration Test
**Status:** ✅ COMPLETE

### 1. Full Stack Smoke Test
A final end-to-end test was performed to verify the stability of all backend services.

**Verification Checklist:**
- [x] **Docker Status:** `rushd_fastapi` and `rushd_qdrant` are active and healthy.
- [x] **GPU Acceleration:** `nvidia-smi` confirmed that the RTX 3050 handles LLM inference (68% load detected during query).
- [x] **VRAM Management:** Peak memory usage was recorded at **3229MiB**, well within the **3.5GB safety limit**.
- [x] **Connectivity:** Verified that the FastAPI container seamlessly communicates with the native Ollama service using the host's IP stack.

---

## 🛠️ Sprint 5.1 & 5.2 — Multilingual Services (IndicTrans2 & Parler-TTS)
**Status:** ✅ COMPLETE

### 1. IndicTrans2 Translation Service
The `ai4bharat/indictrans2-en-indic-1B` model was added as a self-hosted backend translation service. This is not on-device offline translation; the Android app still needs to reach the backend.
- **Service:** `indictrans2` running on port `8001`.
- **Environment:** Created `venv_indictrans2` to isolate its specific dependency versions (e.g., `transformers==4.45.2`).
- **Memory Strategy:** Current source sets `device = "cuda"`, initially loads the model on CPU, moves the model and input tensors to CUDA for inference, then moves the model back to CPU. This path has not been runtime-verified; older CPU-only notes and source comments are stale, and this configuration does not provide a CPU fallback.

### 2. Parler-TTS Speech Service
The `ai4bharat/indic-parler-tts` model was added as a backend speech service. It is not on-device offline TTS; the Android app needs to reach the backend to use this service.
- **Service:** `tts` running on port `8002`.
- **Environment:** Created `venv_tts` to isolate `parler_tts` dependencies.
- **Download Script:** Added `download_tts.py` to securely fetch the gated model using the `.env` Hugging Face Token.
- **Model lifecycle:** The service loads its model and tokenizer during FastAPI startup into CPU memory, moves the model to CUDA for inference, then moves it back to CPU. The model-loading error path and GPU concurrency have not been runtime-verified; there is no global GPU scheduler.

---

## 🛠️ Sprint 4.4 — Advanced RAG Pipeline Optimization
**Status:** ✅ COMPLETE

### 1. RAG Hallucination Mitigation Strategies
The RAG pipeline (`backend/rag_pipeline.py`) was significantly upgraded to reduce LLM hallucinations and increase answer accuracy, implementing three major features:
- **Query Routing:** Intercepts incoming user queries and explicitly asks a lightweight LLM router if the question is related to Islam. If not, it safely rejects it before searching the vector database, preventing off-topic hallucinations.
- **Multi-Query Expansion:** Rewrites the user's single query into three different semantic variations (e.g. asking the same thing in different words or languages). All variations are searched in parallel against Qdrant to ensure broad recall and minimize misses on edge-case terminology.
- **CrossEncoder Reranking:** Takes all the loosely matched vector results and passes them through a deep NLP scorer (`cross-encoder/ms-marco-TinyBERT-L-2-v2`). This reranks the top matches based on actual semantic meaning rather than just cosine distance, ensuring only the most highly relevant fatwas are passed to the LLM.

### 2. Debugging and Robustness
- **Feature Flags:** The `fastapi_server.py` endpoint was updated to accept toggle flags (`enable_routing`, `enable_multi_query`, `enable_reranking`) so the client can enable/disable these features dynamically for testing and performance tuning.
- **Terminal Tracing:** Comprehensive beginner-friendly console logging was added to `rag_pipeline.py`. The terminal now prints the original query, expanded queries, reranker scores, and the actual fatwa text snippets being sent to the LLM.
- **Data Integrity Fixes:** Resolved a critical bug where Deoband fatwa texts were ignored because their Qdrant payloads used the key `question_and_answer` instead of `question`/`answer`. The pipeline now safely checks both formats, ensuring the LLM always receives the full text.

## 📁 File Structure (Backend)
```
backend/
├── Dockerfile
├── docker-compose.yml
├── fastapi_server.py
├── requirements.txt
├── translation_service.py
├── tts_service.py
├── download_tts.py
├── test_gpu_offload.py
├── test_gpu_offload.py
├── stt_service.py
├── requirements_stt.txt
└── local_models/

---

## 🛠️ Sprint 5.3 — faster-whisper STT Service
**Status:** ✅ COMPLETE

### 1. faster-whisper STT Service
To provide highly accurate speech-to-text when connected to the local network, the `faster-whisper-large-v3-turbo` model was containerized.
- **Service:** `stt` running on port `8003`.
- **Environment:** Created `venv_stt` for its specific dependencies including `faster-whisper` and `requests`.
- **Memory Strategy:** The service configures CUDA with `compute_type="int8"` and loads the model on demand for each transcription request, then deletes it and runs garbage collection. This is not proof that the workload always fits the 4GB GPU, and requests are not coordinated by a global GPU scheduler.
- **Android Integration:** The Android app was updated in Phase 5.3 to send captured audio via Retrofit (Multipart) when the network is available. When the network is unavailable, it gracefully falls back to the local `whisper.cpp` JNI execution.
```

---
*Rushd-ul-Ilm — Shaik Hidayatullah, Kurnool, India*


## 5. IndicTrans2 Translation Pipeline Upgrade (July 2026)

### Markdown Preservation Strategy
The initial translation service translated raw text, which destroyed Markdown formatting (like `**bold**`, tables, and lists) generated by the cloud LLMs, leading to broken tables and hallucinated asterisks in Telugu.

To fix this without destroying the grammar via chunking, we implemented a **Hybrid Markdown Parsing Strategy**:
1. **Block-Level Extraction:** Regular expressions detect structural Markdown (e.g., `|---|---|`, `---`) and completely bypass the translation engine for those lines, perfectly preserving table grids and horizontal rules.
2. **Cell-by-Cell Table Translation:** Table rows (e.g., `| Cell A | Cell B |`) are dynamically split by pipes (`|`). Each cell is translated individually, then perfectly re-joined with the pipes.
3. **Inline Tagging (HTML Trick):** Standard Markdown bold/italics (`**word**`) are temporarily converted to HTML (`<b>word</b>`) before translation. Since models like IndicTrans2 are trained on massive amounts of web-scraped data, they natively understand HTML tags and keep them wrapped around the correct translated word without hallucinating. Post-translation, the HTML tags are safely swapped back to Markdown asterisks.

### Historical Hardware Scaling Note (superseded)
An earlier activity-log entry described forcing IndicTrans2 to CPU after an OOM report. The current checked-in implementation instead hard-codes `device = "cuda"`, stages the model in CPU memory, moves it to CUDA for inference, and offloads it afterward. The old CPU-only description and nearby source comments do not match that executable path. Do not treat either the earlier OOM fix or the current offload path as a guarantee against OOM; validate the service on the target hardware.


### Markdown Spacing & Stray Tag Fix
When the translation model processed HTML tags like `<b>Urine</b>`, it would sometimes introduce spaces during generation (e.g. `< b > మూత్రం < / b >`). The original script replaced this directly into `** మూత్రం **`, which broke the Markdown rendering.

*Fix:* The `html_to_md()` regex replacement was upgraded to use a lambda function that calls `.strip()` on the captured text before wrapping it in asterisks, ensuring `**మూత్రం**`. Additionally, a cleanup regex was added to quietly delete any stray or broken HTML tags (like a leftover `</i>` or `<b>`) that the translation model forgot to close properly, ensuring no raw HTML bleeds into the Android app UI.
