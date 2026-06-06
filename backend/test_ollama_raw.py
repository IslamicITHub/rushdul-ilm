import ollama

# File: backend/test_ollama_raw.py
# Test the 'ollama' python library directly

try:
    print("[*] Connecting to Ollama using 'ollama' python library...")
    client = ollama.Client(host="http://localhost:11434")
    response = client.generate(model="qwen3:4b", prompt="Say hello")
    print(f"\n[+] SUCCESS! Response: {response['response']}")
except Exception as e:
    print(f"\n[!] FAILED: {e}")
