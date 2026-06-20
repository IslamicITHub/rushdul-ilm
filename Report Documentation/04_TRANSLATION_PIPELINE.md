# Report Documentation — Phase 5: Translation Pipeline

This document outlines the architecture, setup, and integration tests of the multilingual translation pipeline for the Rushd-ul-Ilm project.

---

## 🏛️ Translation Pipeline Concept
The translation pipeline acts as the multilingual interface of our local AI services. 
* **Purpose:** It translates English inputs to Telugu/Urdu (and vice versa) using locally hosted models to bypass language barriers while keeping all user data private on local hardware.
* **Analogy:** This pipeline is like a bilingual clerk. When a Telugu-speaking user asks a question, the clerk translates it to English (which the RAG search database understands), fetches the answer, and translates the English answer back into Telugu before presenting it to the user.

---

## 🛠️ Service Architecture: IndicTrans2
The translation backend uses the **IndicTrans2** family of models developed by AI4Bharat:
1. `indictrans2-en-indic-1B`: Translates English input to Indic languages (Hindi, Telugu, Urdu, etc.).
2. `indictrans2-indic-en-1B`: Translates Indic languages to English.

### 💾 8-bit Quantization (VRAM Optimization)
To run these models on consumer hardware (such as an NVIDIA RTX 3050 with 4GB VRAM), we load the model weights in **8-bit precision** using the `bitsandbytes` library. 
* **VRAM savings:** Reduces IndicTrans2 VRAM consumption from ~2.2GB to **~1.1GB** per model.
* **Active Swap:** Only one model is held in GPU VRAM at a time. If the translation direction changes, the active model is automatically deleted, garbage collection is triggered, CUDA cache is cleared, and the other model is loaded on-demand.

---

## 🔧 Resolved Issues & Fixes

### 1. Repeating Output (Text Degeneration)
* **Problem:** Translating text was returning looped gibberish (e.g., "అల్ల అల్ల అల్ల...").
* **Cause:** A custom patch in `modeling_indictrans.py` attempting to override the `past_key_values` attention cache was corrupting model predictions on modern library versions.
* **Solution:** Restored the original, clean `modeling_indictrans.py` file from the Hugging Face hub using the verified `HF_TOKEN` from `.env`.

### 2. quantized `.to` ValueError Compatibility
* **Problem:** Loading the quantized model with `BitsAndBytesConfig` threw `ValueError: .to is not supported for 4-bit or 8-bit bitsandbytes models` inside `dispatch_model`.
* **Cause:** Compatibility issues in `transformers==5.12.1` and `python 3.13`.
* **Solution:** Originally downgraded to `transformers==4.46.2`, but this broke the Qwen3 embedding model (`KeyError: 'qwen3'`). We resolved both compatibility issues by upgrading back to **`transformers==5.12.1`** and patching `modeling_indictrans.py` to support `Cache` layer objects:
  - Patched `past_key_values_length` extraction to check `hasattr(past_key_values, "get_seq_length")`.
  - Patched `past_key_value` layer retrieval to check for `DynamicLayer` key/value cache attributes (`cache_obj.layers[idx].keys is not None`) instead of indexing tuples directly.

---

## 🧪 Integration Verification

### 1. English to Telugu translation:
* **Payload:** `{"text": "Quran is the divine speech and revelation of Allah.", "source_lang": "eng_Latn", "target_lang": "tel_Telu"}`
* **Response:** `{"translation":"ఖురాన్ అనేది అల్లాహ్ యొక్క దైవిక ప్రసంగం మరియు ద్యోతకం."}` (Success)

### 2. English to Hindi translation:
* **Payload:** `{"text": "Quran is the divine speech and revelation of Allah.", "source_lang": "eng_Latn", "target_lang": "hin_Deva"}`
* **Response:** `{"translation":"कुरान अल्लाह की दिव्य वाणी और प्रकटीकरण है।"}` (Success)

### 3. Telugu to English translation (Dynamic Model Swap):
* **Payload:** `{"text": "ఖురాన్ అనేది అల్లాహ్ యొక్క దైవిక ప్రసంగం మరియు ద్యోతకం.", "source_lang": "tel_Telu", "target_lang": "eng_Latn"}`
* **Response:** `{"translation":"The Quran is the divine speech and revelation of Allah."}` (Success)

### 4. Telugu Q&A End-to-End Query (FastAPI routing):
* **Payload:** `{"question": "వజూ ఎలా చేయాలి?", "language": "te", "sources": ["all"]}`
* **Workflow:**
  1. FastAPI server on port 8000 receives Telugu input.
  2. Routes translation to port 8001 (`indictrans2` container). Mapped to English.
  3. Queries LlamaIndex vector store (Qdrant) with English query.
  4. Summarizes English answer using NVIDIA NIM / Ollama.
  5. Translates English response back to Telugu via port 8001.
* **Response:** Successfully returns translated Q&A response containing the Telugu explanation, original question, expanded search query, and cited source URLs from Deoband/IslamQA.

### 📊 VRAM Verification
* **Quantized Model Footprint:** **~1.45 GB** VRAM during active translation.
* **Peak GPU VRAM Usage:** **1.92 GB** (with Xorg/Desktop processes active), leaving **~2.1 GB** free for concurrent services.
