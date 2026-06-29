# 📁 File: backend/tts_service.py
#    Why here: This is a standalone Python FastAPI service placed in the backend folder.
#    Relates to: docker-compose.yml (which runs this), and ApiService.kt (Android app which calls this)

# -------------------------------------------------------------------------------------------------
# 📝 WHAT THIS SCRIPT IS:
# This is a web server that listens for Text-To-Speech (TTS) requests from the Android app.
# It uses an Artificial Intelligence model (Parler-TTS) to convert written text into spoken audio.
#
# 💡 REAL-LIFE ANALOGY:
# Imagine this script is a voice actor sitting in a recording booth. The Android app sends a script 
# (the text) to the booth. The actor reads it out loud, records it into an audio file (WAV), and 
# emails it back to the Android app (encoded as Base64 text) so the app can play it to the user.
# -------------------------------------------------------------------------------------------------

import io
# ^ The 'io' module lets Python treat chunks of computer memory exactly like files on a hard drive
import base64
# ^ 'base64' is a translator that converts raw binary data (like an audio file) into plain text characters
import torch
# ^ 'torch' is PyTorch, the engine that powers Artificial Intelligence math on the Graphics Card (GPU)
import scipy.io.wavfile as wavfile
# ^ 'scipy.io.wavfile' is a tool that packages raw audio waves into standard playable .wav files
from fastapi import FastAPI, HTTPException
# ^ 'FastAPI' is the framework that creates our web server, and 'HTTPException' is used to send error codes
from pydantic import BaseModel
# ^ 'BaseModel' helps us define exactly what the incoming JSON request should look like
from parler_tts import ParlerTTSForConditionalGeneration
# ^ The specific AI model class for our Text-To-Speech engine
from transformers import AutoTokenizer
# ^ 'AutoTokenizer' breaks down whole sentences into smaller pieces (tokens) that the AI can understand

from contextlib import asynccontextmanager

device = "cuda" if torch.cuda.is_available() else "cpu"
# ^ We check if an Nvidia GPU is available ('cuda'). If not, we fall back to using the 'cpu'

model_id = "/app/local_models/indic-parler-tts"
# ^ The exact folder path inside our Docker container where the downloaded AI brain is stored

model = None
tokenizer = None

@asynccontextmanager
async def lifespan(app: FastAPI):
# ^ This lifespan function loads the AI brain into memory only when the web server actually starts up
    global model, tokenizer
    print(f"Loading TTS model from {model_id} into CPU memory (for on-demand GPU offloading)...")
    try:
        model = ParlerTTSForConditionalGeneration.from_pretrained(model_id, torch_dtype=torch.float16, low_cpu_mem_usage=True).to("cpu")
        tokenizer = AutoTokenizer.from_pretrained(model_id)
        print("Model loaded successfully.")
    except Exception as e:
        print(f"Failed to load model: {e}")
        model = None
        tokenizer = None
    yield
    # ^ The server handles requests while paused here. When shutting down, it resumes and clears memory.
    model = None
    tokenizer = None

app = FastAPI(title="Rushd-ul-Ilm TTS Service", lifespan=lifespan)
# ^ We create our web server application, give it a title, and attach our memory-loading lifespan

class TTSRequest(BaseModel):
# ^ This defines the shape of the data the Android app will send us
    text: str
    # ^ The actual words we want the AI to speak
    description: str = "A female speaker delivers a clear and neutral speech with a moderate speed."
    # ^ The instructions for the voice actor. Parler-TTS can change voices based on this description!

class TTSResponse(BaseModel):
# ^ This defines the shape of the data we will send BACK to the Android app
    audio_base64: str
    # ^ The final audio file, converted into a long string of text

@app.post("/tts", response_model=TTSResponse)
# ^ We create an endpoint called '/tts'. The Android app will send POST requests here.
async def generate_speech(request: TTSRequest):
# ^ This is the main function that runs whenever the Android app asks for speech
    if model is None or tokenizer is None:
    # ^ First, we check if the AI brain is actually loaded and ready
        raise HTTPException(status_code=500, detail="Model not loaded.")
        # ^ If not, we return a 500 Server Error to the Android app
    
    try:
    # ^ We try to generate the speech safely
        if device == "cuda":
        # ^ If we have a Graphics Card
            model.to(device)
            # ^ Move the massive AI brain from RAM into the Graphics Card (GPU) memory right before we need it

        input_ids = tokenizer(request.description, return_tensors="pt").input_ids.to(device)
        # ^ Convert the voice actor instructions into numbers and move them to the GPU
        prompt_input_ids = tokenizer(request.text, return_tensors="pt").input_ids.to(device)
        # ^ Convert the actual text to be spoken into numbers and move them to the GPU
        
        generation = model.generate(input_ids=input_ids, prompt_input_ids=prompt_input_ids)
        # ^ The AI thinks really hard and generates raw audio sound waves (as numbers)
        
        audio_arr = generation.to(torch.float32).cpu().numpy().squeeze()
        # ^ We convert the audio numbers from float16 to float32, pull them off the GPU, and clean up the array shape
        
        if device == "cuda":
        # ^ Now that the AI is done generating the speech...
            model.to("cpu")
            # ^ Move the massive AI brain OUT of the Graphics Card and back into regular RAM
            del input_ids
            # ^ Delete the leftover instruction numbers from the Graphics Card
            del prompt_input_ids
            # ^ Delete the leftover text numbers from the Graphics Card
            del generation
            # ^ Delete the raw audio waves from the Graphics Card
            import gc
            # ^ Import the Garbage Collector
            gc.collect()
            # ^ Force Python to immediately delete all abandoned variables in memory
            torch.cuda.empty_cache()
            # ^ Tell the Graphics Card to fully empty its trash bin, freeing up space for the Translation model!
        
        byte_io = io.BytesIO()
        # ^ Create a fake 'file' completely inside the computer's memory
        
        wavfile.write(byte_io, model.config.sampling_rate, audio_arr)
        # ^ Package the raw audio waves into a standard .wav file and write it into our fake memory file
        
        byte_io.seek(0)
        # ^ Rewind the fake memory file back to the beginning so we can read it
        
        audio_b64 = base64.b64encode(byte_io.read()).decode('utf-8')
        # ^ Read the .wav file, translate it into Base64 plain text, and convert it to a standard string
        
        return TTSResponse(audio_base64=audio_b64)
        # ^ Send the Base64 text string all the way back to the Android app!
        
    except Exception as e:
    # ^ If the AI crashes while generating speech (like Out of Memory)
        raise HTTPException(status_code=500, detail=str(e))
        # ^ Send the error back to the Android app

@app.get("/health")
# ^ We create an endpoint called '/health'
def health_check():
# ^ This function is just a quick heartbeat check to see if the server is alive
    if model is not None:
    # ^ If the AI brain is loaded
        return {"status": "healthy"}
        # ^ Tell anyone who asks that we are healthy
    return {"status": "unhealthy"}
    # ^ Tell anyone who asks that we are sick

if __name__ == "__main__":
# ^ This line checks if this script is being run directly (not imported by another script)
    import uvicorn
    # ^ Uvicorn is the actual web server engine that runs FastAPI
    uvicorn.run(app, host="0.0.0.0", port=8002)
    # ^ Start the server on port 8002, accessible from any IP address
