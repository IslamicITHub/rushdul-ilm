# Import the FastAPI class to create our web application instance
from fastapi import FastAPI
from pydantic import BaseModel
import requests
from contextlib import asynccontextmanager

# Import our custom RAG pipeline logic
from rag_pipeline import RagPipeline

# 1. Define a global variable placeholder for the pipeline
rag = None

# 2. Create a lifespan context manager
@asynccontextmanager
async def lifespan(app: FastAPI):
    # This code runs exactly once, right before the server starts accepting requests
    global rag
    print("\n[*] INITIALIZING BACKEND...")
    print("[*] Loading AI models and connecting to vector database. This may take a moment...")
    
    # Initialize the heavy pipeline here
    rag = RagPipeline()
    
    print("[+] Models loaded successfully. Server is ready!")
    
    # The 'yield' pauses execution here while the app runs
    yield
    
    # Any code after 'yield' runs when you shut the server down
    print("\n[*] Shutting down server and releasing resources...")

# 3. Pass the lifespan to the FastAPI app instance
app = FastAPI(
    title="Rushd-ul-Ilm API",
    description="Backend server for the Islamic Q&A App",
    version="1.0.0",
    lifespan=lifespan
)

class QueryRequest(BaseModel):
    question: str

@app.get("/health")
def health_check():
    return {"status": "healthy", "message": "Rushd-ul-Ilm Backend is running."}

@app.post("/query")
def ask_question(request: QueryRequest):
    # Call the globally loaded RAG pipeline
    try:
        result = rag.ask(request.question)
        return result
    except Exception as e:
        return {"error": f"RAG Pipeline Error: {str(e)}"}
