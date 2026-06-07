import os
from huggingface_hub import snapshot_download

# Enable fast parallel downloading
os.environ["HF_HUB_ENABLE_HF_TRANSFER"] = "1"

# Define where you want to save the model locally on your PC
LOCAL_MODEL_DIR = "./local_models/qwen3_embedding_06b_local"

print(f"Starting download of Qwen3-Embedding-0.6B into: {LOCAL_MODEL_DIR}")

# Download the repository files cleanly
snapshot_download(
    repo_id="Qwen/Qwen3-Embedding-0.6B",
    local_dir=LOCAL_MODEL_DIR,
    ignore_patterns=["*.msgpack", "*.h5", "*.ot"] # Ignore non-PyTorch weights to save disk space
)

print("Download complete! You can now disconnect from the internet.")
