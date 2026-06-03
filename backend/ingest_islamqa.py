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
# We use 'paraphrase-multilingual-MiniLM-L12-v2' (420MB) instead of MPNET (1.1GB)
# to avoid download timeouts, while still supporting Telugu and Urdu.
MODEL_NAME = 'paraphrase-multilingual-MiniLM-L12-v2'
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

if __name__ == "__main__":
    ingest()
