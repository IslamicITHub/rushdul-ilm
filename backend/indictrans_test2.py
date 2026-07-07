import os
import re
import sys
import torch
import warnings

# Suppress environment logs and warnings
os.environ["PYTHONWARNINGS"] = "ignore"
os.environ["TOKENIZERS_PARALLELISM"] = "false"
warnings.simplefilter("ignore")

from transformers import AutoModelForSeq2SeqLM, AutoTokenizer, logging
logging.set_verbosity_error()
from IndicTransToolkit import IndicProcessor

# Path to your local model
model_name = "./local_models/indictrans2-en-indic-1B"

print("Loading tokenizer and model...")
tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
model = AutoModelForSeq2SeqLM.from_pretrained(
    model_name, trust_remote_code=True, torch_dtype=torch.float16, device_map="auto"
)
ip = IndicProcessor(inference=True)

def translate_markdown(markdown_content, src_lang="eng_Latn", tgt_lang="hin_Deva"):
    lines = markdown_content.split("\n")
    translated_lines = [""] * len(lines)
    
    in_code_block = False
    blocks_to_translate = []
    block_metadata = []  # Stores tuple: (line_index, prefix_syntax)

    for i, line in enumerate(lines):
        # 1. Skip code blocks entirely
        if line.strip().startswith("```"):
            in_code_block = not in_code_block
            translated_lines[i] = line
            continue
        if in_code_block:
            translated_lines[i] = line
            continue
        if not line.strip():
            translated_lines[i] = line
            continue

        # 2. Extract Headings (#, ##, etc.)
        heading_match = re.match(r'^(#{1,6}\s+)(.*)$', line)
        if heading_match:
            blocks_to_translate.append(heading_match.group(2))
            block_metadata.append((i, heading_match.group(1)))
            continue

        # 3. Extract Unordered Lists (- item, * item)
        ul_match = re.match(r'^(\s*[-*+]\s+)(.*)$', line)
        if ul_match:
            blocks_to_translate.append(ul_match.group(2))
            block_metadata.append((i, ul_match.group(1)))
            continue

        # 4. Extract Ordered Lists (1. item, 2. item)
        ol_match = re.match(r'^(\s*\d+\.\s+)(.*)$', line)
        if ol_match:
            blocks_to_translate.append(ol_match.group(2))
            block_metadata.append((i, ol_match.group(1)))
            continue

        # 5. Regular paragraph text block
        blocks_to_translate.append(line)
        block_metadata.append((i, ""))

    # Batch process all collected text chunks simultaneously
    if blocks_to_translate:
        batch = ip.preprocess_batch(blocks_to_translate, src_lang=src_lang, tgt_lang=tgt_lang, visualize=False)
        inputs = tokenizer(batch, padding="longest", truncation=True, max_length=256, return_tensors="pt").to("cuda")
        
        with torch.inference_mode():
            outputs = model.generate(
                **inputs, use_cache=True, min_length=0, max_length=256,
                num_beams=5, num_return_sequences=1, repetition_penalty=1.2, no_repeat_ngram_size=3
            )
        
        generated_tokens = tokenizer.batch_decode(outputs.detach().cpu().tolist(), skip_special_tokens=True)
        translations = ip.postprocess_batch(generated_tokens, lang=tgt_lang)

        # Reconstruct markdown structure using preserved metadata
        for (line_idx, prefix), translation in zip(block_metadata, translations):
            translated_lines[line_idx] = f"{prefix}{translation}"

    return "\n".join(translated_lines)


# --- RUNNING DATA VERIFICATION ---

sample_markdown = """Shaykh al-Islam Ibn Taymiyah (may **Allah** have mercy on him) said: Whoever was paid for **selling** an unlawful item or doing an unlawful service such as carrying wine, or who received a fee for making a cross or a fee for prostitution and the like should give it in charity and repent from that unlawful action, and his giving in charity what he had earned thereby will be an expiation for what he did, because it is not permissible for him to make use of it, as it is unlawful earnings. And it should not be given back to the one who paid him, because he has benefited from the service in question; rather he should give it in charity as some of the scholars stated, and as Imam Ahmad stated in a similar case of one who carried wine, and as the companions of Malik and others also stated."""

print("\n--- TRANSLATING TO HINDI ---")
hindi_output = translate_markdown(sample_markdown, src_lang="eng_Latn", tgt_lang="hin_Deva")
print(hindi_output)

print("\n--- TRANSLATING TO TELUGU ---")
telugu_output = translate_markdown(sample_markdown, src_lang="eng_Latn", tgt_lang="tel_Telu")
print(telugu_output)
