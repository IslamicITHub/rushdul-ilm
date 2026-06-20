import torch
from transformers import AutoModelForSeq2SeqLM

print(f"Initial reserved: {torch.cuda.memory_reserved() / 1024**2} MB")
model_name = "./local_models/indictrans2-en-indic-1B"
model = AutoModelForSeq2SeqLM.from_pretrained(model_name, torch_dtype=torch.float16, trust_remote_code=True).to("cpu")
print(f"Loaded to CPU. Reserved: {torch.cuda.memory_reserved() / 1024**2} MB")

model.to("cuda")
print(f"Moved to GPU. Reserved: {torch.cuda.memory_reserved() / 1024**2} MB")

model.to("cpu")
import gc
gc.collect()
torch.cuda.empty_cache()
print(f"Moved to CPU and cleared. Reserved: {torch.cuda.memory_reserved() / 1024**2} MB")
