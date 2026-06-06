# File: backend/ingest_islamqa.py
# Purpose: Converts IslamQA SQLite fatwas into vector embeddings and saves them in Qdrant.
# Layer: Phase 3 — Knowledge Ingestion
# Created: 2026-06-03 | Modified: 2026-06-03
# Developer: Shaik Hidayatullah

import sqlite3
import json
import os
from qdrant_client import QdrantClient
from qdrant_client.http import models
from sentence_transformers import SentenceTransformer

# --- CONFIGURATION ---
# Assuming 'local_models' is in the root of your backend directory
# It's safer to use absolute paths to avoid working-directory issues in Docker
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_NAME = os.path.join(BASE_DIR, "local_models", "paraphrase-multilingual-MiniLM-L12-v2")
# We use 'paraphrase-multilingual-MiniLM-L12-v2' (420MB) instead of MPNET (1.1GB)
# to avoid download timeouts, while still supporting Telugu and Urdu.
#MODEL_NAME = 'paraphrase-multilingual-MiniLM-L12-v2'
VEC_SIZE = 384

SQLITE_DB_PATH = "/data/islamqa/islamqa_database.sqlite"
QDRANT_HOST = "localhost"
QDRANT_PORT = 6333
COLLECTION_NAME = "islamqa"

def ingest():
    print(f"[*] Connecting to Qdrant at {QDRANT_HOST}:{QDRANT_PORT}...")
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)

    print(f"[*] Loading embedding model: {MODEL_NAME}...")
    model = SentenceTransformer(MODEL_NAME)

    print(f"[*] Setting up Qdrant collection: {COLLECTION_NAME}...")
    client.recreate_collection(
        collection_name=COLLECTION_NAME,
        vectors_config=models.VectorParams(size=VEC_SIZE, distance=models.Distance.COSINE),
    )

    if not os.path.exists(SQLITE_DB_PATH):
        print(f"[!] Error: SQLite database not found at {SQLITE_DB_PATH}")
        return

    conn = sqlite3.connect(SQLITE_DB_PATH)
    cursor = conn.cursor()

    print("[*] Fetching fatwas from SQLite...")
    cursor.execute("SELECT id, title, question, url FROM fatwas")
    rows = cursor.fetchall()
    print(f"[+] Found {len(rows)} fatwas.")

    batch_size = 100
    for i in range(0, len(rows), batch_size):
        batch = rows[i : i + batch_size]
        texts_to_embed = [f"{row[1]} {row[2]}" for row in batch]
        
        print(f"    [*] Generating embeddings for batch {i//batch_size + 1}...")
        embeddings = model.encode(texts_to_embed)
        
        points = []
        for idx, row in enumerate(batch):
            f_id, f_title, f_question, f_url = row
            points.append(
                models.PointStruct(
                    id=f_id,
                    vector=embeddings[idx].tolist(),
                    payload={
                        "title": f_title,
                        "url": f_url,
                        "source": "IslamQA.info",
                        "text": f_question[:2000]
                    }
                )
            )
        
        print(f"    [*] Upserting batch {i//batch_size + 1} to Qdrant...")
        client.upsert(collection_name=COLLECTION_NAME, points=points)

    print(f"\n[SUCCESS] Ingestion complete. {len(rows)} fatwas are now in Qdrant!")
    conn.close()
def test_search():
    # 1. Connect to your already running, already populated Qdrant container
    client = QdrantClient(host="localhost", port=6333)
    
    # 2. Load the exact same AI model you used during ingestion
    # (Since it's already downloaded on your system, this loads instantly)
    model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
    
    # 3. Define a brand new query string you want to test
    query_text = "What breaks or invalidates the fast in ramadan?"
    
    print(f"[*] Translating query into numbers: '{query_text}'")
    # Convert your sentence into a list of 384 floating-point numbers
    query_vector = model.encode(query_text).tolist()
    
    print("[*] Searching the 'islamqa' collection...")
    # 4. Fire the search request against your existing collection
    search_response = client.query_points(
        collection_name="islamqa",
        query=query_vector,
        limit=3  # Fetch the top 3 closest matching fatwas
    )
    search_results = search_response.points
    # 5. Display the matching data stored in Qdrant's payload
    print("\n" + "="*60)
    print("MATCHING RESULTS FOUND IN QDRANT DB:")
    print("="*60)
    
    for rank, hit in enumerate(search_results, start=1):
        print(f"Rank {rank} (Confidence Score: {hit.score:.4f}):")
        print(f"  Question : {hit.payload.get('question')}")
        print(f"  URL   : {hit.payload.get('url')}")
        #print(f"  Answer  : {hit.payload.get('answer')[:2000]}...")
        print(f"  Answer  : {hit.payload.get('answer')}...")
        print("-" * 60)
if __name__ == "__main__":
#    ingest()
	test_search()
