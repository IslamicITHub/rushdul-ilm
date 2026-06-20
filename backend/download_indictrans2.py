import os
from huggingface_hub import snapshot_download

# This script downloads the IndicTrans2 models from HuggingFace
# directly into the local_models directory.

# Load the HF_TOKEN from .env if present
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

hf_token = os.environ.get("HF_TOKEN")
if hf_token == "your_hugging_face_free_API_key" or not hf_token:
    print("WARNING: HF_TOKEN is not set correctly in .env. The ai4bharat models are gated.")
    print("Please accept the license on HuggingFace and set HF_TOKEN in backend/.env")

local_dir = "./local_models"

models = [
    "ai4bharat/indictrans2-en-indic-1B",
    "ai4bharat/indictrans2-indic-en-1B"
]

print("Starting download of IndicTrans2 models...")

for model in models:
    print(f"\nDownloading {model}...")
    model_path = os.path.join(local_dir, model.split("/")[-1])
    
    # We use local_dir_use_symlinks=False to ensure actual files are downloaded
    snapshot_download(
        repo_id=model, 
        local_dir=model_path, 
        local_dir_use_symlinks=False,
        token=hf_token,
        ignore_patterns=["*.msgpack", "*.h5", "rust_model.ot"] # Optional: ignore alternative weight formats to save space
    )
    print(f"Finished downloading {model} to {model_path}")

print("\nAll downloads complete!")
