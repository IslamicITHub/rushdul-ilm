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

device = "cuda"
# ^ Forced to CPU (16GB RAM) to avoid CUDA Out-of-Memory crashes with the 4GB RTX 3050, as requested by developer

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
        # ^ We keep it in float32 because CPU processors handle float32 math natively and much faster than float16
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
    
    import re
    # ^ Import regex module to identify purely structural markdown lines
    
    # --- HELPER FUNCTIONS FOR INLINE MARKDOWN ---
    def md_to_html(text):
    # ^ Convert Markdown asterisks to HTML tags before translating so the AI understands it's formatting
        text = re.sub(r'\*\*(.*?)\*\*', r'<b>\1</b>', text)
        text = re.sub(r'\*(.*?)\*', r'<i>\1</i>', text)
        return text

    def html_to_md(text):
    # ^ Convert HTML tags back to Markdown asterisks after translating and clean up AI-introduced spaces
        text = re.sub(r'<\s*b\s*>(.*?)<\s*/\s*b\s*>', lambda m: f'**{m.group(1).strip()}**' if m.group(1).strip() else '', text, flags=re.IGNORECASE)
        text = re.sub(r'<\s*i\s*>(.*?)<\s*/\s*i\s*>', lambda m: f'*{m.group(1).strip()}*' if m.group(1).strip() else '', text, flags=re.IGNORECASE)
        # ^ Remove spaces inside the bold/italic tags and drop them completely if they are empty
        text = re.sub(r'<\/?\s*[bi]\s*>', '', text, flags=re.IGNORECASE)
        # ^ Remove any leftover broken/stray HTML tags that the AI translator left behind
        return text
    # --------------------------------------------
    
    # 1. SPLIT TEXT BY LINES TO PRESERVE MARKDOWN & AVOID TRUNCATION
    original_lines = request.text.split('\n')
    # ^ Split the massive LLM answer by newlines to preserve Markdown lists and paragraphs
    
    translation_jobs = []
    # ^ We will store (line_index, cell_index, prefix, text_to_translate, suffix)
    
    for i, line in enumerate(original_lines):
        if not line.strip() or re.match(r'^[\s\|\-]+$', line):
            continue
            # ^ Skip empty lines and purely structural lines (like table borders)
            
        if line.strip().startswith('|') and line.strip().endswith('|'):
        # ^ If it's a Markdown table row...
            cells = line.split('|')
            # ^ Split the row into individual cells so we don't break the pipes '|'
            for j, cell in enumerate(cells):
                if cell.strip():
                    text_to_translate = md_to_html(cell.strip())
                    # ^ Protect inline bold/italics by converting to HTML
                    translation_jobs.append((i, j, "", text_to_translate, ""))
        else:
        # ^ If it's a normal line...
            match = re.match(r'^(\s*(?:[-*+]|\d+\.|>|#{1,6})\s+)(.*)$', line)
            # ^ Extract block prefixes like "- ", "1. ", "> ", "### " so the AI doesn't see them
            if match:
                prefix = match.group(1)
                core_text = match.group(2)
            else:
                prefix = ""
                core_text = line
                
            text_to_translate = md_to_html(core_text)
            translation_jobs.append((i, -1, prefix, text_to_translate, ""))
            
    translated_pieces = {}
    CHUNK_SIZE = 1
    # ^ Process 2 chunks at a time so we don't blow up the 4GB RTX 3050 VRAM

    if device == "cuda":
    # ^ Right before the heavy lifting starts...
        model.to(device)
        # ^ Move the massive AI brain from regular RAM into the Graphics Card (GPU)
        
    for idx in range(0, len(translation_jobs), CHUNK_SIZE):
    # ^ Loop through the text chunks
        chunk = translation_jobs[idx:idx+CHUNK_SIZE]
        texts = [job[3] for job in chunk]
        # ^ Grab the safe, pure English HTML text
        
        batch = ip.preprocess_batch(
        # ^ Clean the incoming text chunk before the AI sees it
            texts,
            src_lang=request.source_lang,
            tgt_lang=request.target_lang,
        )
        
        inputs = tokenizer(
        # ^ Convert the cleaned text words into math numbers
            batch, 
            truncation=True,
            max_length=512,
            # ^ Allow up to 512 tokens per line (solves the "incomplete translation" issue)
            padding="longest",
            return_tensors="pt",
            return_attention_mask=True
        ).to(device)
        
        with torch.no_grad():
        # ^ Tell PyTorch NOT to track math gradients (saves a massive amount of memory)
            outputs = model.generate(
            # ^ Force the AI to generate the translated numbers
                **inputs, 
                max_new_tokens=512,
                # ^ Force it to translate the full line completely without stopping early
                use_cache=True,
                min_length=0,
                num_beams=1,
                # ^ Keep beam search low to save VRAM
                num_return_sequences=1,
            )
            
        generated_tokens = tokenizer.batch_decode(
        # ^ Convert the math numbers back into actual human words
            outputs,
            skip_special_tokens=True,
            clean_up_tokenization_spaces=True,
        )
        
        translations = ip.postprocess_batch(generated_tokens, lang=request.target_lang)
        # ^ Polish the translated grammar
        
        for job, trans in zip(chunk, translations):
            i, j, prefix, _, suffix = job
            trans_md = html_to_md(trans)
            # ^ Convert the safe HTML tags back into Markdown asterisks
            final_piece = prefix + trans_md + suffix
            # ^ Re-attach the extracted prefixes (like "- " or "> ")
            translated_pieces[(i, j)] = final_piece

    if device == "cuda":
    # ^ Now that ALL translation chunks are finished...
        model.to("cpu")
        # ^ Move the massive brain OUT of the Graphics Card and back to regular RAM
        import gc
        gc.collect()
        torch.cuda.empty_cache()
        # ^ Empty the Graphics Card trash bin completely, freeing space!
        
    # 2. REASSEMBLE THE MARKDOWN TEXT
    final_lines = []
    for i, original_line in enumerate(original_lines):
        if not original_line.strip() or re.match(r'^[\s\|\-]+$', original_line):
        # ^ If it was an empty line or a table border, keep it exactly as it was
            final_lines.append(original_line)
            continue
            
        if original_line.strip().startswith('|') and original_line.strip().endswith('|'):
        # ^ If it was a table row, reconstruct the cells carefully
            cells = original_line.split('|')
            new_cells = []
            for j, cell in enumerate(cells):
                if cell.strip():
                    new_cells.append(" " + translated_pieces[(i, j)] + " ")
                else:
                    new_cells.append(cell)
            final_lines.append('|'.join(new_cells))
        else:
        # ^ If it was a normal line, just grab the translated piece
            final_lines.append(translated_pieces[(i, -1)])

    translated_text = '\n'.join(final_lines)
    # ^ Glue the translated lines back together with newlines
    
    return {"translation": translated_text}
    # ^ Send the completely preserved, translated Markdown text back to the Android app

@app.get("/health")
# ^ We create a simple '/health' endpoint
async def health_check():
# ^ A function to check if the server is alive
    status = "healthy" if len(models) > 0 else "models_not_loaded"
    # ^ Consider the server healthy only if at least one brain is loaded
    return {"status": status, "loaded_models": list(models.keys())}
    # ^ Return the status and a list of which brains are currently taking up memory
