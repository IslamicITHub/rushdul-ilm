# File: download_model.py
from sentence_transformers import SentenceTransformer
import os

# Define where you want to save the model locally
save_directory = "./local_models/paraphrase-multilingual-MiniLM-L12-v2"

print("Downloading model...")
# This fetches it from Hugging Face
model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")

print(f"Saving model to {save_directory}...")
# This saves the raw weights and configuration files locally
model.save(save_directory)

print("Done! You can now use this model offline.")
