# File: backend/stt_service.py
# Purpose: FastAPI wrapper for faster-whisper STT service
# Layer: Layer 2 — Backend Docker Services
# Created: 2026-06-27 | Developer: AI Agent

import os
import io
import time
import numpy as np
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from faster_whisper import WhisperModel

from contextlib import asynccontextmanager

# We specify the model size and configuration
#MODEL_SIZE = "deepdml/faster-whisper-large-v3-turbo-ct2"
MODEL_SIZE = "./local_models/faster-whisper-large-v3-turbo-ct2"
# ^ We use the large-v3-turbo model, which is highly accurate and very fast

@asynccontextmanager
async def lifespan(app: FastAPI):
    # We no longer load the model globally to save 1.5GB VRAM.
    # It will be loaded on-demand inside the endpoint to allow sharing the GPU.
    yield

# Initialize FastAPI application
app = FastAPI(title="Faster Whisper STT API", lifespan=lifespan)
# ^ Starts the FastAPI app which handles incoming HTTP requests

@app.get("/health")
async def health_check():
    """Returns the health status of the STT service."""
    # ^ Simple endpoint to verify the service is running
    return {"status": "healthy", "service": "faster-whisper STT"}

@app.post("/transcribe")
async def transcribe_audio(file: UploadFile = File(...)):
    """Transcribes an uploaded audio file using faster-whisper."""
    # ^ Endpoint to process speech-to-text
    try:
        start_time = time.time()
        # ^ Track processing time
        
        # Read the uploaded file bytes
        audio_bytes = await file.read()
        # ^ Reads the incoming audio stream into memory
        
        print(f"Loading faster-whisper model: {MODEL_SIZE} in INT8 mode on demand...")
        model = WhisperModel(MODEL_SIZE, device="cuda", compute_type="int8", download_root="/app/local_models")
        # ^ Loads the model into VRAM just in time for inference
        
        # Determine if it's a WAV file or raw PCM float32 based on content type or filename
        if file.filename.endswith(".pcm") or file.filename.endswith(".raw") or file.content_type == "application/octet-stream":
            # ^ If raw PCM float32 (16000 Hz, mono)
            audio_array = np.frombuffer(audio_bytes, dtype=np.float32)
            # ^ Convert bytes directly to numpy array
            #segments, info = model.transcribe(audio_array, beam_size=5, language="en", condition_on_previous_text=False)
            segments, info = model.transcribe(audio_array, beam_size=5, task="translate", condition_on_previous_text=False)
            # ^ Run inference on the raw array
        else:
            # ^ If it's a normal WAV/MP3 file
            audio_io = io.BytesIO(audio_bytes)
            # ^ Wrap bytes in a file-like object
            #segments, info = model.transcribe(audio_io, beam_size=5, language="en", condition_on_previous_text=False)
            segments, info = model.transcribe(audio_io, beam_size=5, task="translate", language="ur", condition_on_previous_text=False)
            # ^ Run inference on the audio file
            
        # Combine all transcribed segments into one single text block
        transcription = " ".join([segment.text for segment in segments])
        # ^ Loop through all spoken segments and join them
        
        processing_time = time.time() - start_time
        # ^ Calculate total time taken
        
        print(f"Transcription complete in {processing_time:.2f}s: {transcription}")
        # ^ Log the result
        
        # Clean up memory explicitly
        import gc
        del model
        # ^ Delete the model to free up the 1.5GB of VRAM for other services
        if 'audio_bytes' in locals(): del audio_bytes
        if 'audio_array' in locals(): del audio_array
        if 'audio_io' in locals(): del audio_io
        gc.collect()
        
        return JSONResponse(content={
            "transcription": transcription.strip(),
            "language": info.language,
            "language_probability": info.language_probability,
            "processing_time": processing_time
        })
        # ^ Return the transcribed text as JSON
        
    except Exception as e:
        # ^ Catch any errors (e.g. invalid audio format)
        print(f"Error during transcription: {e}")
        raise HTTPException(status_code=500, detail=str(e))
        # ^ Return HTTP 500 with the error message

################################################
# The below code logic is used to test whether the model is properly transcribing the audio .wav file to its respective language text
################################################

# This block only runs if you execute this script directly (not if imported elsewhere)
if __name__ == "__main__":
    # Import TestClient to simulate HTTP requests without starting a live web server
    from fastapi.testclient import TestClient
    import sys
    
    # Initialize the test client with your FastAPI 'app' instance
    client = TestClient(app)
    
    # Define the name of the test audio file you want to process
    # Make sure to place a real Urdu WAV file with this exact name in the same folder
    test_file_path = f"{sys.argv[1]}"
    
    print(f"\n--- Starting Instant Local Test ---")
    
    # Check if the audio file actually exists on your drive to prevent crash errors
    if not os.path.exists(test_file_path):
        print(f"❌ Error: Could not find '{test_file_path}'. Please add the audio file to this directory.")
        sys.exit(1) # Exit the script safely if the file is missing
        
    print(f"✅ Found '{test_file_path}'. Simulating upload to /transcribe endpoint...")
    
    # Open the audio file in binary read mode ("rb") so we can send the raw bytes
    with open(test_file_path, "rb") as audio_file:
        # Simulate a POST request to your /transcribe endpoint
        # 'files' parameter mimics a multipart/form-data upload from a frontend client
        response = client.post(
            "/transcribe",
            files={"file": (test_file_path, audio_file, "audio/wav")}
        )
    
    # Print the HTTP status code to confirm if the request was successful (200 is good)
    print(f"HTTP Status Code: {response.status_code}")
    
    # If the API returned a 200 OK, print the parsed JSON dictionary
    if response.status_code == 200:
        print("\n--- Transcription Results ---")
        # .json() parses the JSONResponse returned by your endpoint into a Python dict
        print(response.json()) 
    else:
        # If it failed (e.g. 500 internal server error), print the error text
        print(f"\n❌ Test Failed! Error details:\n{response.text}") 

