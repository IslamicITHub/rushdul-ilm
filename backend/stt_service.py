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

# Initialize FastAPI application
app = FastAPI(title="Faster Whisper STT API")
# ^ Starts the FastAPI app which handles incoming HTTP requests

# We specify the model size and configuration
MODEL_SIZE = "deepdml/faster-whisper-large-v3-turbo-ct2"
# ^ We use the large-v3-turbo model, which is highly accurate and very fast

print(f"Loading faster-whisper model: {MODEL_SIZE} in INT8 mode...")
# Load the model directly into the GPU using int8 precision to save VRAM (fits in 1.5GB)
model = WhisperModel(MODEL_SIZE, device="cuda", compute_type="int8", download_root="/app/local_models")
# ^ Loads the model into VRAM
print("Model loaded successfully!")

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
        
        # If it's a raw PCM float32 array (which Android sends), we could parse it directly.
        # But faster-whisper transcribe() can also take an io.BytesIO if it's a valid media file (WAV).
        # We will assume it's a valid WAV/media file for standard compatibility.
        # If Android sends raw PCM without WAV header, we will need to convert it to a numpy float32 array.
        
        # Determine if it's a WAV file or raw PCM float32 based on content type or filename
        if file.filename.endswith(".pcm") or file.filename.endswith(".raw") or file.content_type == "application/octet-stream":
            # ^ If raw PCM float32 (16000 Hz, mono)
            audio_array = np.frombuffer(audio_bytes, dtype=np.float32)
            # ^ Convert bytes directly to numpy array
            segments, info = model.transcribe(audio_array, beam_size=5, language="en", condition_on_previous_text=False)
            # ^ Run inference on the raw array
        else:
            # ^ If it's a normal WAV/MP3 file
            audio_io = io.BytesIO(audio_bytes)
            # ^ Wrap bytes in a file-like object
            segments, info = model.transcribe(audio_io, beam_size=5, language="en", condition_on_previous_text=False)
            # ^ Run inference on the audio file
            
        # Combine all transcribed segments into one single text block
        transcription = " ".join([segment.text for segment in segments])
        # ^ Loop through all spoken segments and join them
        
        processing_time = time.time() - start_time
        # ^ Calculate total time taken
        
        print(f"Transcription complete in {processing_time:.2f}s: {transcription}")
        # ^ Log the result
        
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
