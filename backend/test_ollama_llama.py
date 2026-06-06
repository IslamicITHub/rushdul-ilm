from llama_index.llms.ollama import Ollama

# File: backend/test_ollama_llama.py
# Diagnostic script to test LlamaIndex + Ollama connection

try:
    print("[*] Connecting to Ollama at http://localhost:11434 with model qwen3:4b...")
    llm = Ollama(model="qwen3:4b", base_url="http://localhost:11434", request_timeout=60.0, context_window=4096)
    print(f"[*] LlamaIndex model attribute is: {llm.model}")

    print("[*] Sending 'complete' request...")
    response = llm.complete("Hello! Please confirm your model size.")
    print(f"\n[+] SUCCESS! Response: {response}")
except Exception as e:
    print(f"\n[!] FAILED: {e}")
