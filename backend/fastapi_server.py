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

from contextlib import asynccontextmanager

rag = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global rag
    rag = RagPipeline()
    yield
    rag = None

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
    version="1.0.0",
    # ^ Sets the current API version string
    lifespan=lifespan
)
# ^ Ends FastAPI instantiation

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
    language: str = "en"
    # ^ The user's language selection tag (e.g. "te" for Telugu), defaults to English
# ^ Ends QueryRequest class declaration

TRANSLATION_API_URL = "http://localhost:8001/translate"
# ^ The network address mapping connections to our local IndicTrans2 translation endpoint

def translate_text(text: str, source_lang: str, target_lang: str) -> str:
    """
    Translates text by calling our local IndicTrans2 translation service on port 8001.
    If translation fails, it falls back to returning the original text segment safely.
    """
    try:
    # ^ Starts try block to catch network timeout or API offline errors gracefully
        lang_map = {
        # ^ Declares map linking simple client tags to standard FLORES-200 tags
            "te": "tel_Telu",
            # ^ Maps Telugu 'te' locale to 'tel_Telu'
            "ur": "urd_Arab",
            # ^ Maps Urdu 'ur' locale to 'urd_Arab'
            "hi": "hin_Deva",
            # ^ Maps Hindi 'hi' locale to 'hin_Deva'
            "en": "eng_Latn"
            # ^ Maps English 'en' locale to 'eng_Latn'
        }
        # ^ Ends language tag mapping dictionary
        src_tag = lang_map.get(source_lang, source_lang)
        # ^ Resolves the source tag from language tag dictionary
        tgt_tag = lang_map.get(target_lang, target_lang)
        # ^ Resolves the target tag from language tag dictionary
        
        if src_tag == tgt_tag:
        # ^ Checks if the resolved source and target tags match (e.g. en to en)
            return text
            # ^ Returns the original text immediately without wasting API request calls
            
        payload = {
        # ^ Declares payload request dictionary matching translation service schemas
            "text": text,
            # ^ Passes text payload to be translated
            "source_lang": src_tag,
            # ^ Mapped source language tag
            "target_lang": tgt_tag
            # ^ Mapped target language tag
        }
        # ^ Ends payload mapping
        
        response = requests.post(TRANSLATION_API_URL, json=payload, timeout=120)
        # ^ Performs the HTTP POST network request to port 8001 and waits for response
        print(f"[DEBUG] HTTP response of Translation endpoint :\n{response}")
        # This print statement is added for debugging purposes.
        
        if response.status_code == 200:
        # ^ Checks if the API returned success code 200
            return response.json().get("translation", text)
            # ^ Retrieves and returns the translated text from response payload
        else:
        # ^ Executes if translation API returns failure codes
            print(f"Translation API error: {response.text}")
            # ^ Prints error details to terminal logs
            return text
            # ^ Returns original text as safe fallback
    except Exception as e:
    # ^ Catches socket errors if translation container is offline
        print(f"Failed to translate: {str(e)}")
        # ^ Prints exception debug trace to stdout console
        return text
        # ^ Returns original text segment as fallback
# ^ Ends translate_text helper function

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
        sources = request.sources
        # ^ Extracts database sources filter list
        if sources and sources[0] == "all":
        # ^ Checks if the filter contains target "all" keyword
            sources = ['islamqa', 'deoband']
            # ^ Automatically expands to search all approved database collections
            
        # 🏛️ Translate incoming query question to English
        english_question = translate_text(request.question, source_lang=request.language, target_lang="en")
        # ^ Translates input query to English (standard search query format for LlamaIndex)
        print(f"Original: {request.question} | Translated: {english_question}")
        # ^ Prints debug comparison logs
        
        # Query the RAG pipeline using translated English question
        result = rag.ask(english_question, chat_history=request.chat_history, sources=sources)
        # ^ Queries LlamaIndex database mapping using English search query
        
        # 🏛️ Translate English generated answer back to user's preferred language
        english_answer = result.get("answer")
        # ^ Extracts the generated answer text segment
        if english_answer:
        # ^ Checks if answer text is valid
            translated_answer = translate_text(english_answer, source_lang="en", target_lang=request.language)
            # ^ Translates English answer back to user's language (e.g. Telugu)
        else:
        # ^ Fallback if no answer text exists
            translated_answer = None
            # ^ Marks answer as null
            
        return {
        # ^ Compiles query results JSON object sent back to Android client
            "answer": translated_answer,
            # ^ Mapped translated answer string
            "question": request.question,
            # ^ Original user question string (keeps UI synced)
            "expanded_search_query": result.get("expanded_search_query"),
            # ^ Technical query string used for matching
            "sources": result.get("sources", []),
            # ^ Mapped citations links list
            "is_clarification": result.get("is_clarification", False),
            # ^ Flag marking classification triggers
            "error": None
            # ^ Error indicator status field
        }
        # ^ Ends return mapping
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
