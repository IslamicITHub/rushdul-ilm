# File: backend/fastapi_server.py
# Purpose: Main backend server for the Rushd-ul-Ilm Android app.
# Layer: Backend Docker Services
# Depends on: fastapi, uvicorn, rag_pipeline.py
# Created: 2026-06-02 | Modified: 2026-06-11
# Developer: Shaik Hidayatullah

from fastapi import FastAPI
# ^ Import the FastAPI class to create our web application instance
from pydantic import BaseModel
# ^ Import BaseModel to define the 'shape' of data we expect from the Android app
import requests
# ^ Import requests to let our Python script talk to the Ollama AI server

from rag_pipeline import RagPipeline
# ^ Import our custom RAG pipeline logic from the local directory

# 🏛️ CONCEPT: FastAPI runs an asynchronous web server that exposes REST endpoints for the client.
#    Pydantic models act as schemas that validate the shape of JSON requests and responses.
# 🏛️ ANALOGY: fastapi_server.py is like a front-desk receptionist in a medical clinic.
#    It takes the patient's intake form (QueryRequest), checks if it is filled correctly,
#    passes it to the doctor (RagPipeline) in the back room, and returns the doctor's prescription back to the patient.
app = FastAPI(
# ^ Create the main FastAPI application instance.
    title="Rushd-ul-Ilm API",
    # ^ Sets the display name of the API documentation
    description="Backend server for the Islamic Q&A App",
    # ^ Provides a description of what this backend API manages
    version="1.0.0"
    # ^ Sets the current API version string
)
# ^ Ends FastAPI instantiation

rag = RagPipeline()
# ^ Initialize the RAG pipeline globally so it's ready when requests come in (loads models on startup)

# 🏛️ CONCEPT: Pydantic models define static data structures that ensure type safety.
#    Any incoming HTTP request must match this structure or it will be rejected automatically.
# 🏛️ ANALOGY: QueryRequest is like a custom mail envelope with printed fields for "Question", "Chat History", and "Sources".
#    If the sender leaves the question empty or uses the wrong format, the post office rejects the envelope immediately.
class QueryRequest(BaseModel):
# ^ Declares QueryRequest class inheriting from Pydantic's BaseModel
    question: str
    # ^ The user's question text string (transcribed from audio or typed)
    chat_history: list = []
    # ^ An optional list of dictionaries representing previous chat turns for contextual Q&A
    sources: list = ["islamqa", "deoband"]
    # ^ Filter list containing approved Islamic source domains, defaulting to all
# ^ Ends QueryRequest class declaration

@app.get("/health")
# ^ Decorator declaring a GET request route on the "/health" endpoint path
def health_check():
# ^ Function handler to perform backend system health checking
    return {"status": "healthy", "message": "Rushd-ul-Ilm Backend is running."}
    # ^ Returns a dictionary representing JSON response confirming server availability
# ^ Ends health_check function handler

@app.post("/query")
# ^ Decorator declaring a POST request route on the "/query" endpoint path
def ask_question(request: QueryRequest):
# ^ Function handler to process user question requests, receiving QueryRequest data structure
    try:
    # ^ Starts try block to catch any runtime processing exceptions safely
            if request.sources[0] == "all":
            # ^ Checks if the first item in source list is the keyword "all"
                request.sources = ['islamqa', 'deoband']
                # ^ Expands the source filter list to query both approved websites
                result = rag.ask(request.question, chat_history=request.chat_history, sources=request.sources)
                # ^ Queries RAG pipeline with user question, conversation history, and source filters
                return result
                # ^ Returns RAG answer and reference details JSON back to client
            else:
            # ^ Handles case where specific source filters are requested
                result = rag.ask(request.question, chat_history=request.chat_history, sources=request.sources)
                # ^ Queries RAG pipeline with specific source filters
                return result
                # ^ Returns RAG answer details JSON back to client
    except Exception as e:
    # ^ Catches any runtime exceptions raised during query processing
        return {
        # ^ Returns a structured JSON error response to prevent Android application crashes
            "answer": None,
            # ^ Marks answer as null/None
            "sources": [],
            # ^ Returns empty sources list
            "is_clarification": False,
            # ^ Marks clarification flag as false
            "error": f"RAG Pipeline Error: {str(e)}"
            # ^ Passes the error details string for debugging assistance
        }
        # ^ Ends error JSON mapping
# ^ Ends ask_question function handler
