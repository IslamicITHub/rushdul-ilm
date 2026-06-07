# ----------------------------------------------------------------------------
# Rushd-ul-Ilm (رشد العلم) — FastAPI Dockerfile
# 
# What is this file?
# A Dockerfile is like a recipe. It tells Docker exactly how to build the 
# "shipping container" for our Python API. It starts with a base operating 
# system, copies our code into it, and installs the required libraries.
# ----------------------------------------------------------------------------

# 1. Base Image: Start with a small, lightweight version of Linux that 
# already has Python 3.11 installed. 'slim' means it removes unnecessary files.
FROM python:3.11-slim

# 2. Working Directory: Create a folder inside the container called /app
# and make it the current active directory. All following commands run here.
WORKDIR /app

# 3. Copy Requirements: Copy the requirements.txt file from your host machine
# into the /app folder inside the container.
COPY requirements.txt .

# 4. Install Dependencies: Run 'pip install' inside the container to download
# and install FastAPI, Uvicorn, Requests, etc. 
# We break this into steps so Docker can cache the smaller libraries if the large ones fail.
RUN pip install --default-timeout=1000 --no-cache-dir fastapi uvicorn requests pydantic qdrant-client

# Install Torch (CPU version) separately as it is the largest file (~500MB)
RUN pip install --default-timeout=1000 --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

# Finally install sentence-transformers and LlamaIndex components
RUN pip install --default-timeout=1000 --no-cache-dir \
    sentence-transformers \
    llama-index-core \
    llama-index-vector-stores-qdrant \
    llama-index-llms-ollama \
    llama-index-embeddings-huggingface \
    llama-index-llms-openai-like \
    

# 5. Copy Code: Copy all the rest of our Python code (like fastapi_server.py)
# from the host machine into the /app folder in the container.
COPY . .

# 6. Expose Port: Tell Docker that this container will listen for network 
# traffic on port 8000 (which is the default port for FastAPI).
EXPOSE 8000

# 7. Start Command: This is the command that runs automatically when the 
# container starts. It tells Uvicorn to run the 'app' variable found inside 
# 'fastapi_server.py'.
# '--host 0.0.0.0' allows external connections (like from our Android app).
CMD ["uvicorn", "fastapi_server:app", "--host", "0.0.0.0", "--port", "8000"]
