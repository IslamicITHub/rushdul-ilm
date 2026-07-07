#!/usr/bin/env python3
# File: test_nvidia_llm.py
# Purpose: Standalone CLI testing script for NVIDIA NIM Cloud LLM inference.
# Outputs: Displays response in terminal and writes to gpt_output.md.

import os
import sys
import argparse
from openai import OpenAI

# 🌐 API CONFIGURATIONS
# Using the same base URL and default model defined in rag_pipeline.py
NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1"
DEFAULT_MODEL = "openai/gpt-oss-20b"

def run_inference(user_prompt: str, system_prompt: str, model_name: str):
    """
    Sends prompts to the NVIDIA NIM Cloud API and handles the response.
    """
    # 1. Validate environment credentials
    api_key = os.environ.get("NVIDIA_API_KEY")
    if not api_key or api_key == "$NVIDIA_API_KEY":
        print("[!] ERROR: NVIDIA_API_KEY environment variable is missing or invalid.")
        print("    Please export your key: export NVIDIA_API_KEY='nvapi-...'")
        sys.exit(1)

    print(f"[*] Initializing connection to NVIDIA NIM API ({NVIDIA_BASE_URL})...")
    
    # 2. Initialize OpenAI-compatible client for NVIDIA
    client = OpenAI(
        base_url=NVIDIA_BASE_URL,
        api_key=api_key
    )

    # 3. Assemble message payload
    messages = []
    if system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    
    messages.append({"role": "user", "content": user_prompt})

    print(f"[*] Sending request to model: {model_name}...")
    print(f"[*] User Prompt: '{user_prompt}'\n")

    try:
        # 4. Execute API completion call
        completion = client.chat.completions.create(
            model=model_name,
            messages=messages,
            temperature=0.0,  # Keeping zero temperature as per your RAG rules
            max_tokens=4096,
            stream=False
        )

        # Extract generated text
        ai_response = completion.choices[0].message.content.strip()
        
        # 5. Output to Terminal
        print("=" * 60)
        print("--- NVIDIA LLM INFERENCE RESPONSE ---")
        print("=" * 60)
        print(ai_response)
        print("=" * 60)

        # 6. Save to gpt_output.md
        output_filename = "gpt_output.md"
        with open(output_filename, "w", encoding="utf-8") as f:
            f.write(f"# AI Inference Output\n\n")
            f.write(f"**Model Used:** `{model_name}`  \n")
            if system_prompt:
                f.write(f"**System Prompt:** *{system_prompt}* \n")
            f.write(f"**User Prompt:** *{user_prompt}* \n\n")
            f.write(f"## Response\n\n")
            f.write(ai_response)
            f.write("\n")

        print(f"\n[+] Successfully saved output to {os.path.abspath(output_filename)}")

    except Exception as e:
        print(f"\n[!] API Error occurred during inference: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    # Configure CLI Argument Parser
    parser = argparse.ArgumentParser(
        description="Test NVIDIA Cloud LLM API inference and save output to markdown."
    )
    
    parser.add_argument(
        "-u", "--user-prompt",
        type=str,
        required=True,
        help="The primary question or task instruction for the LLM."
    )
    
    parser.add_argument(
        "-s", "--system-prompt",
        type=str,
        default="You are a helpful, accurate, and concise AI assistant. Provide your answers in clean Markdown format.",
        help="Optional system instructions to define agent behavior and rules."
    )
    
    parser.add_argument(
        "-m", "--model",
        type=str,
        default=DEFAULT_MODEL,
        help=f"Target NVIDIA model name (default: {DEFAULT_MODEL})."
    )

    args = parser.parse_args()

    # Trigger inference pipeline
    run_inference(
        user_prompt=args.user_prompt,
        system_prompt=args.system_prompt,
        model_name=args.model
    )
