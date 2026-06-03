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

# Create the main FastAPI application instance. 
# This 'app' variable is the engine that handles all incoming requests.
app = FastAPI(
    title="Rushd-ul-Ilm API",
    description="Backend server for the Islamic Q&A App",
    version="1.0.0"
)

# Define a "Data Model" for the query request.
# This tells FastAPI exactly what the Android app must send us.
class QueryRequest(BaseModel):
    # We expect a JSON object with a 'question' field which must be a string.
    question: str

# Create an endpoint for HTTP GET requests at the "/health" URL path.
# The Android app will use this to quickly check if the server is reachable.
@app.get("/health")
def health_check():
    # When a request comes to /health, we return this simple JSON dictionary.
    # It tells the caller that the server is alive and functioning properly.
    return {"status": "healthy", "message": "Rushd-ul-Ilm Backend is running."}

# Create a new endpoint for HTTP POST requests at the "/query" URL path.
# This is where the Android app sends the user's spoken question.
@app.post("/query")
def ask_question(request: QueryRequest):
    # 1. Define the address of our native Ollama AI server.
    # Because we are using 'network_mode: host', the container can reach
    # the host's localhost directly.
    ollama_url = "http://localhost:11434/api/generate"
    
    # 2. Prepare the data we want to send to the AI (Ollama).
    # We specify the 'qwen3:4b' model and pass the user's question.
    # 'stream': False means we want the full answer at once, not word-by-word.
    payload = {
        "model": "qwen3:4b",
        "prompt": request.question,
        "stream": False
    }
    
    # 3. Send the request to Ollama and wait for the answer.
    # This is like making a phone call to the AI and waiting for it to speak.
    response = requests.post(ollama_url, json=payload)
    
    # 4. Check if the AI answered correctly (status code 200 means success).
    if response.status_code == 200:
        # Convert the AI's response from JSON into a Python dictionary.
        result = response.json()
        # Return the actual text answer back to the Android app.
        return {"answer": result.get("response", "No response from AI.")}
    else:
        # If something went wrong, return an error message.
        return {"error": "Failed to reach Ollama AI server."}
