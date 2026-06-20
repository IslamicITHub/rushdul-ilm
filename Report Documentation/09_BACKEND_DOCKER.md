# Report Documentation — Phase 2: Backend Docker Services

This document tracks the progress and implementation details of the backend services for the Rushd-ul-Ilm project.

## 🚀 Phase 2 Overview
**Goal:** All local AI services (Ollama, Qdrant, FastAPI) running in Docker containers with GPU acceleration enabled.

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
- **Live Reload:** The local `backend` directory was bind-mounted to `/app` inside the container, and the startup command was set to run Uvicorn with the `--reload` flag. This allows Python code changes to take effect immediately without rebuilding the container.

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
To support Telugu and Hindi translations offline, the `ai4bharat/indictrans2-en-indic-1B` model was containerized.
- **Service:** `indictrans2` running on port `8001`.
- **Environment:** Created `venv_indictrans2` to isolate its specific dependency versions (e.g., `transformers==4.45.2`).
- **Memory Strategy:** Implemented a **Dynamic GPU Offloading** mechanism. Because the RTX 3050 only has 4GB of VRAM, the model loads into CPU RAM by default. It moves to the GPU (`.to("cuda")`) right before translating, and returns to the CPU (`.to("cpu")`) immediately after. Aggressive garbage collection (`del inputs`, `gc.collect()`) and `torch.cuda.empty_cache()` are used to free the VRAM for the TTS service.

### 2. Parler-TTS Speech Service
To synthesize high-quality speech offline, the `ai4bharat/indic-parler-tts` model was containerized.
- **Service:** `tts` running on port `8002`.
- **Environment:** Created `venv_tts` to isolate `parler_tts` dependencies.
- **Download Script:** Added `download_tts.py` to securely fetch the gated model using the `.env` Hugging Face Token.
- **Memory Strategy:** Utilizes the same Dynamic GPU Offloading orchestrator as the translation service. A typecast was added to convert the `float16` output tensor to `float32`, which is required by `scipy.io.wavfile` to serialize the audio correctly into a base64 WAV response.

---

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
└── local_models/
```

---
*Rushd-ul-Ilm — Shaik Hidayatullah, Kurnool, India*
