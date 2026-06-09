# File: backend/fastapi_server.py
# Purpose: Main backend server for the Rushd-ul-Ilm Android app.
# Layer: Backend Docker Services
# Depends on: fastapi, uvicorn
# Created: 2026-06-02
# Developer: Shaik Hidayatullah

# Import the FastAPI class to create our web application instance
from fastapi import FastAPI
# Import BaseModel to define the 'shape' of data we expect from the Android app
from pydantic import BaseModel
# Import requests to let our Python script talk to the Ollama AI server
import requests

# Import our custom RAG pipeline logic
from rag_pipeline import RagPipeline

# Create the main FastAPI application instance. 
app = FastAPI(
    title="Rushd-ul-Ilm API",
    description="Backend server for the Islamic Q&A App",
    version="1.0.0"
)

# Initialize the RAG pipeline globally so it's ready when requests come in.
# This loads the AI models into memory once on startup.
rag = RagPipeline()

# Define a "Data Model" for the query request.
# We include 'chat_history' as an optional list to support multi-turn clarifying questions.
class QueryRequest(BaseModel):
    question: str
    chat_history: list = [] # Format: [{"role": "user", "content": "..."}, {"role": "assistant", "content": "..."}]
    sources: list = ["islamqa", "deoband"] # Filter for specific sources (default to all)

@app.get("/health")
def health_check():
    return {"status": "healthy", "message": "Rushd-ul-Ilm Backend is running."}

@app.post("/query")
def ask_question(request: QueryRequest):
    # Call the RAG pipeline with both the current question and the history.
    try:
            if request.sources[0] == "all":
                request.sources = ['islamqa', 'deoband']
                result = rag.ask(request.question, chat_history=request.chat_history, sources=request.sources)
                return result
            else:
                result = rag.ask(request.question, chat_history=request.chat_history, sources=request.sources)
                return result
    except Exception as e:
        # ⚠️ Return a structured error so the Android app doesn't crash
        return {
            "answer": None,
            "sources": [],
            "is_clarification": False,
            "error": f"RAG Pipeline Error: {str(e)}"
        }
