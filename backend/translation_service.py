# 📁 File: backend/translation_service.py
#    Why here: This is a standalone Python FastAPI service placed in the backend folder.
#    Relates to: docker-compose.yml (which runs this), and the Android App / FastAPI Server

# -------------------------------------------------------------------------------------------------
# 📝 WHAT THIS SCRIPT IS:
# This service acts as our offline language translator using the IndicTrans2 AI model.
# It translates text back and forth between English and Indian languages (like Telugu and Hindi)
# completely locally, without needing an internet connection.
#
# 💡 REAL-LIFE ANALOGY:
# Imagine this script is a bilingual receptionist at a hospital. If a patient speaks Telugu,
# the receptionist translates their question into English so the main doctor (the AI LLM) can 
# understand it. When the doctor answers in English, the receptionist translates it back to Telugu
# before handing it to the patient.
# -------------------------------------------------------------------------------------------------

from fastapi import FastAPI, HTTPException
# ^ 'FastAPI' builds our web server, 'HTTPException' allows us to send standard internet error codes (like 404 or 500)
from pydantic import BaseModel
# ^ 'BaseModel' helps us define exactly what the incoming JSON request should look like
import torch
# ^ 'torch' is PyTorch, the engine that powers AI math on the Graphics Card (GPU)
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
# ^ 'transformers' provides the blueprints for loading our specific AI translation model

app = FastAPI(title="IndicTrans2 Translation API")
# ^ We create our web server application and give it a title

class TranslationRequest(BaseModel):
# ^ This defines the shape of the data the Android app (or main server) will send us
    text: str
    # ^ The actual sentence we want translated
    source_lang: str  
    # ^ The language the text is currently in (e.g., 'eng_Latn' for English, 'tel_Telu' for Telugu)
    target_lang: str  
    # ^ The language we want the text to become

models = {}
# ^ A blank dictionary to store the loaded AI brains in computer memory
tokenizers = {}
# ^ A blank dictionary to store the AI dictionaries (tokenizers) in memory

device = "cuda" if torch.cuda.is_available() else "cpu"
# ^ Check if an Nvidia GPU is available ('cuda'). If not, fall back to using the 'cpu'

def load_model(direction: str):
# ^ A function to load the massive AI brain into memory
    model_name = f"/app/local_models/indictrans2-{direction}-1B"
    # ^ Construct the exact folder path where the downloaded model is stored
    
    print(f"Loading {model_name} onto CPU in float16 precision (for on-demand GPU offloading)...")
    # ^ Print a status message so the developer knows the server is working
    
    tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
    # ^ Load the tokenizer (which breaks sentences into tiny pieces the AI understands)
    
    model = AutoModelForSeq2SeqLM.from_pretrained(
    # ^ Load the actual translation AI brain
        model_name,
        # ^ Pass the folder path
        torch_dtype=torch.float16,
        # ^ Shrink the brain's numbers to float16 so it takes up half the space (fitting in 4GB VRAM)
        trust_remote_code=True
        # ^ Allow custom code from the AI creators to run
    ).to("cpu")
    # ^ Initially force the massive brain to sit in regular RAM (CPU) to keep the Graphics Card empty
    
    model.eval()  
    # ^ Set the brain to 'eval' (evaluation) mode, which means it will only translate, not learn/train
    
    models[direction] = model
    # ^ Save the loaded brain into our dictionary
    tokenizers[direction] = tokenizer
    # ^ Save the loaded dictionary into our dictionary
    print(f"{model_name} loaded successfully!")
    # ^ Print a success message

@app.post("/translate")
# ^ We create an endpoint called '/translate' for incoming requests
async def translate(request: TranslationRequest):
# ^ The main function that runs when a translation request arrives
    if request.source_lang == "eng_Latn":
    # ^ Check if we are translating FROM English
        direction = "en-indic"
        # ^ We need the English-to-Indic brain
    elif request.target_lang == "eng_Latn":
    # ^ Check if we are translating TO English
        direction = "indic-en"
        # ^ We need the Indic-to-English brain
    else:
    # ^ If English isn't involved at all (which our specific model doesn't support directly)
        raise HTTPException(status_code=400, detail="Translation must involve English as either source or target.")
        # ^ Reject the request and tell the caller why
    
    if direction not in models or direction not in tokenizers:
    # ^ If the specific brain we need isn't currently loaded into memory...
        other_dir = "indic-en" if direction == "en-indic" else "en-indic"
        # ^ Figure out what the *other* brain is called
        
        if other_dir in models:
        # ^ If the *other* brain is currently hogging memory...
            print(f"Unloading {other_dir} to free up VRAM...")
            # ^ Print that we are kicking it out
            del models[other_dir]
            # ^ Delete the old brain
            del tokenizers[other_dir]
            # ^ Delete the old dictionary
            import gc
            # ^ Import the Garbage Collector
            gc.collect()
            # ^ Force Python to clean up the abandoned memory
            if device == "cuda":
            # ^ If we have a Graphics Card...
                torch.cuda.empty_cache()
                # ^ Empty the Graphics Card trash bin completely
                
        load_model(direction)
        # ^ Now that we have space, load the brain we actually need
        
    tokenizer = tokenizers[direction]
    # ^ Grab the ready tokenizer
    model = models[direction]
    # ^ Grab the ready brain
    
    from IndicTransToolkit.processor import IndicProcessor
    # ^ Import a special toolkit that cleans up Indian text (fixes grammar spacing, etc.)
    ip = IndicProcessor(inference=True)
    # ^ Start the toolkit in 'inference' (translation) mode
    
    batch = ip.preprocess_batch(
    # ^ Clean the incoming text before the AI sees it
        [request.text],
        # ^ The text to clean
        src_lang=request.source_lang,
        # ^ Tell the toolkit what language it currently is
        tgt_lang=request.target_lang,
        # ^ Tell the toolkit what language it will become
    )
    # ^ Save the cleaned text into a 'batch'
    
    if device == "cuda":
    # ^ Right before the heavy lifting starts...
        model.to(device)
        # ^ Move the massive AI brain from regular RAM into the Graphics Card (GPU) for super-fast math

    inputs = tokenizer(
    # ^ Convert the cleaned text words into math numbers
        batch, 
        # ^ The cleaned text
        truncation=True,
        # ^ Chop off text if it's too long
        padding="longest",
        # ^ Add blank spaces if it's too short
        return_tensors="pt",
        # ^ Return PyTorch tensors (math grids)
        return_attention_mask=True
        # ^ Tell the AI which parts of the grid are real words and which are blank padding
    ).to(device)
    # ^ Move these math numbers directly into the Graphics Card
    
    with torch.no_grad():
    # ^ Tell PyTorch NOT to track math gradients (saves a massive amount of memory since we aren't training)
        outputs = model.generate(
        # ^ Force the AI to think and generate the translated numbers
            **inputs, 
            # ^ Pass in our prepared numbers
            max_length=256,
            # ^ Don't generate more than 256 words
            use_cache=True,
            # ^ Speed up the process by remembering past thoughts
            min_length=0,
            # ^ Don't force it to keep talking if it's done
            num_beams=5,
            # ^ Make the AI explore 5 different possible translations simultaneously to find the best sounding one
            num_return_sequences=1,
            # ^ Only return the absolute best 1 translation out of the 5
        )
        # ^ Save the generated numbers into 'outputs'
        
    if device == "cuda":
    # ^ Now that the translation is finished...
        model.to("cpu")
        # ^ Move the massive brain OUT of the Graphics Card and back to regular RAM
        del inputs
        # ^ Delete the input numbers from the Graphics Card
        outputs = outputs.to("cpu")
        # ^ Move the final translated numbers back to regular RAM
        import gc
        # ^ Import the garbage collector
        gc.collect()
        # ^ Force Python to clean up
        torch.cuda.empty_cache()
        # ^ Empty the Graphics Card trash bin completely, freeing space for TTS or Ollama!
        
    generated_tokens = tokenizer.batch_decode(
    # ^ Convert the math numbers back into actual human words
        outputs,
        # ^ The generated numbers
        skip_special_tokens=True,
        # ^ Hide behind-the-scenes AI tags like <end_of_sentence>
        clean_up_tokenization_spaces=True,
        # ^ Fix weird spaces around punctuation
    )
    # ^ Save the human words
    
    translations = ip.postprocess_batch(generated_tokens, lang=request.target_lang)
    # ^ Run the text through the Indian toolkit one last time to polish the grammar
    translated_text = translations[0]
    # ^ Grab the first (and only) translation from the list
    
    return {"translation": translated_text}
    # ^ Send the final translated text back to whoever asked for it

@app.get("/health")
# ^ We create a simple '/health' endpoint
async def health_check():
# ^ A function to check if the server is alive
    status = "healthy" if len(models) > 0 else "models_not_loaded"
    # ^ Consider the server healthy only if at least one brain is loaded
    return {"status": status, "loaded_models": list(models.keys())}
    # ^ Return the status and a list of which brains are currently taking up memory
