# File: backend/ingest_deoband.py
# Purpose: Converts Deoband SQLite fatwas into vector embeddings and saves them in Qdrant.
# Layer: Phase 3 — Knowledge Ingestion
# Created: 2026-06-06 | Modified: 2026-06-06
# Developer: Shaik Hidayatullah

import sqlite3
import os
from qdrant_client import QdrantClient
from qdrant_client.http import models
from sentence_transformers import SentenceTransformer

# --- CONFIGURATION ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# Use the local model folder to avoid re-downloading
MODEL_NAME = os.path.join(BASE_DIR, "local_models", "qwen3_embedding_06b_local")
VEC_SIZE = 1024

# This path is relative to the root of the repo if running locally, 
# or /data/deoband/... if running inside Docker.
SQLITE_DB_PATH = "../darulifta-deoband.com-offline/offline_darulifta2/offline.sqlite"
QDRANT_HOST = "localhost"
QDRANT_PORT = 6333
COLLECTION_NAME = "deoband"

def ingest():
    print(f"[*] Connecting to Qdrant at {QDRANT_HOST}:{QDRANT_PORT}...")
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)

    print(f"[*] Loading embedding model: {MODEL_NAME}...")
    # 'SentenceTransformer' converts sentences into numbers (vectors)
    # We force device='cpu' to avoid CUDA OOM issues while Ollama is running.
    model = SentenceTransformer(MODEL_NAME, device="cuda")

    print(f"[*] Setting up Qdrant collection: {COLLECTION_NAME} (384 dim)...")
    # recreating the collection clears old data and starts fresh
    client.recreate_collection(
        collection_name=COLLECTION_NAME,
        vectors_config=models.VectorParams(size=VEC_SIZE, distance=models.Distance.COSINE),
    )

    if not os.path.exists(SQLITE_DB_PATH):
        print(f"[!] Error: SQLite database not found at {SQLITE_DB_PATH}")
        return

    # Connect to the SQLite database where we saved the scraped fatwas
    conn = sqlite3.connect(SQLITE_DB_PATH)
    cursor = conn.cursor()

    print("[*] Fetching fatwas from SQLite...")
    # Fetch title, question, answer, and URL to embed them
    cursor.execute("SELECT url, answer FROM fatwas")
    rows = cursor.fetchall()
    print(f"[+] Found {len(rows)} fatwas in Deoband database.")

    batch_size = 6
    for i in range(0, len(rows), batch_size):
        batch = rows[i : i + batch_size]
        # We combine title and question for better search context, handling None values
        texts_to_embed = [f"{row[0] or ''} {row[1] or ''}" for row in batch]
        
        print(f"    [*] Generating embeddings for batch {i//batch_size + 1}...")
        embeddings = model.encode(texts_to_embed)
        
        points = []
        for idx, row in enumerate(batch):
            f_url, f_question_and_answer = row
            question_and_answer_text = f_question_and_answer or ""
            points.append(
                models.PointStruct(
                    id=i + idx, # Using simple integer ID for points
                    vector=embeddings[idx].tolist(),
                    payload={
                        "url": f_url,
                        "source": "Darul Ifta Deoband (Hanafi)",
                        "question_and_answer": question_and_answer_text
                    }
                )
            )
        
        print(f"    [*] Upserting batch {i//batch_size + 1} to Qdrant...")
        client.upsert(collection_name=COLLECTION_NAME, points=points)

    print(f"\n[SUCCESS] Deoband Ingestion complete. {len(rows)} fatwas are now in Qdrant (384 dim)!")
    conn.close()

def test_search():
    # 1. Connect to your already running, already populated Qdrant container
    client = QdrantClient(host="localhost", port=6333)
    
    # 2. Load the exact same AI model you used during ingestion
    # (Since it's already downloaded on your system, this loads instantly)
    model = SentenceTransformer(MODEL_NAME)
    
    # 3. Define a brand new query string you want to test
    query_text = "What breaks or invalidates the fast in ramadan?"
    
    print(f"[*] Translating query into numbers: '{query_text}'")
    # Convert your sentence into a list of 384 floating-point numbers
    query_vector = model.encode(query_text).tolist()
    
    print("[*] Searching the 'Deoband' collection...")
    # 4. Fire the search request against your existing collection
    search_response = client.query_points(
        collection_name=COLLECTION_NAME,
        query=query_vector,
        limit=10  # Fetch the top 3 closest matching fatwas
    )
    search_results = search_response.points
    # 5. Display the matching data stored in Qdrant's payload
    print("\n" + "="*60)
    print("MATCHING RESULTS FOUND IN QDRANT DB:")
    print("="*60)
    
    for rank, hit in enumerate(search_results, start=1):
        print(f"Rank {rank} (Confidence Score: {hit.score:.4f}):")
        #print(f"  Question : {hit.payload.get('question')}")
        print(f"  URL   : {hit.payload.get('url')}")
        #print(f"  Answer  : {hit.payload.get('answer')[:2000]}...")
        print(f"  Answer  : {hit.payload.get('question_and_answer')}...")
        print("-" * 60)
if __name__ == "__main__":
    #ingest()
	test_search()
