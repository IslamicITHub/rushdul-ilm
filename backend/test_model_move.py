import torch
from transformers import AutoTokenizer
from parler_tts import ParlerTTSForConditionalGeneration

model_id = "./local_models/indic-parler-tts"
device = "cuda"

print("Loading to CPU...")
model = ParlerTTSForConditionalGeneration.from_pretrained(model_id, torch_dtype=torch.float16).to("cpu")
tokenizer = AutoTokenizer.from_pretrained(model_id)

print("Moving to GPU...")
model.to(device)
text = "Hello"
description = "A female speaker delivers a clear and neutral speech."
input_ids = tokenizer(description, return_tensors="pt").input_ids.to(device)
prompt_input_ids = tokenizer(text, return_tensors="pt").input_ids.to(device)

print("Generating...")
try:
    generation = model.generate(input_ids=input_ids, prompt_input_ids=prompt_input_ids)
    print("Generated successfully!")
except Exception as e:
    print("Error during generate:", e)

print("Moving back to CPU...")
model.to("cpu")
torch.cuda.empty_cache()
print("Done.")
