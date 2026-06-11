# File: backend/ingest_islamqa.py
# Purpose: Converts IslamQA SQLite fatwas into vector embeddings and saves them in Qdrant.
# Layer: Phase 3 — Knowledge Ingestion
# Depends on: sqlite3, os, qdrant_client, sentence_transformers
# Created: 2026-06-03 | Modified: 2026-06-11
# Developer: Shaik Hidayatullah

import sqlite3
# ^ Import sqlite3 to query offline fatwas database records
import os
# ^ Import os to handle absolute directories and environment variables
from qdrant_client import QdrantClient
# ^ Client class driving network requests directly to self-hosted Qdrant server collections
from qdrant_client.http import models
# ^ Imports payload models for structured vector database operations
from sentence_transformers import SentenceTransformer
# ^ SentenceTransformer loads vector embedding models to convert text into mathematical coordinates

# --- CONFIGURATION ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# ^ Extracts the absolute directory path of this file to prevent filepath bugs inside Docker containers
MODEL_NAME = os.path.join(BASE_DIR, "local_models", "qwen3_embedding_06b_local")
# ^ Chains file paths to target the locally downloaded Qwen multilingual embedding folder
VEC_SIZE = 1024
# ^ Enforces vector size parameter to match 1024 dimensions of embedding model
SQLITE_DB_PATH = "../islamqa.info-offline/islamqa_database.sqlite"
# ^ Filepath pointing to local SQLite database containing offline IslamQA fatwa scraping data
QDRANT_HOST = "localhost"
# ^ Default network address name mapping connection routes to the local Qdrant server
QDRANT_PORT = 6333
# ^ The port address where Qdrant database listens for incoming client requests
COLLECTION_NAME = "islamqa"
# ^ Target collection namespace inside Qdrant database for IslamQA fatwas

# 🏛️ CONCEPT: Knowledge ingestion pipelines convert unstructured text documents into vector databases.
#    This process loops through SQLite database records, generates vector arrays, and upserts them to collections.
# 🏛️ ANALOGY: Ingesting IslamQA fatwas is like cataloging a set of fatwa reference books.
#    We pick up each volume (SQLite Row), read the contents (Texts), assign a numeric index code (Embeddings),
#    and slide it into a labeled shelf section (Qdrant IslamQA collection).
def ingest():
# ^ Declares ingest function orchestrating data indexing operations
    print(f"[*] Connecting to Qdrant at {QDRANT_HOST}:{QDRANT_PORT}...")
    # ^ Outputs status update to console log
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)
    # ^ Establishes client driver connection targeting Qdrant server port

    print(f"[*] Loading embedding model: {MODEL_NAME}...")
    # ^ Outputs status update to console log
    model = SentenceTransformer(MODEL_NAME, device='cuda')
    # ^ Instantiates embedding translator model running on CUDA GPU for faster ingestion processing

    print(f"[*] Setting up Qdrant collection: {COLLECTION_NAME}...")
    # ^ Outputs status update to console log
    client.recreate_collection(
    # ^ Deletes existing collection target and creates a clean index structure
        collection_name=COLLECTION_NAME,
        # ^ Target collection name
        vectors_config=models.VectorParams(size=VEC_SIZE, distance=models.Distance.COSINE),
        # ^ Configures 1024 vector dimensions and Cosine metric formula for semantic distance measurements
    )
    # ^ Ends recreate_collection call

    if not os.path.exists(SQLITE_DB_PATH):
    # ^ Validates if SQLite database exists at target path
        print(f"[!] Error: SQLite database not found at {SQLITE_DB_PATH}")
        # ^ Outputs error notification to console log
        return None, None
        # ^ Returns None tuple to indicate failure to caller
    # ^ Ends path validation

    conn = sqlite3.connect(SQLITE_DB_PATH)
    # ^ Opens SQLite connection database reference
    cursor = conn.cursor()
    # ^ Instantiates cursor engine to execute database SQL statements

    print("[*] Fetching fatwas from SQLite...")
    # ^ Outputs status update to console log
    cursor.execute("SELECT id, url, question, answer FROM fatwas")
    # ^ Executes SQL query selecting ID, URL, question, and answer text columns
    rows = cursor.fetchall()
    # ^ Gathers all matching records from SQLite database into memory
    print(f"[+] Found {len(rows)} fatwas.")
    # ^ Prints count of fetched database rows to console log

    batch_size = 1
    # ^ Sets batch size processing count to limit RAM spikes during embedding generation
    for i in range(0, len(rows), batch_size):
    # ^ Loops through the rows in chunks of batch_size steps
        batch = rows[i : i + batch_size]
        # ^ Extracts current chunk subset slice of rows
        texts_to_embed = [f"{row[2]} {row[3]}" for row in batch]
        # ^ Combines question and answer text strings for embedding context
        
        print(f"    [*] Generating embeddings for batch {i//batch_size + 1}...")
        # ^ Outputs chunk status updates to console log
        embeddings = model.encode(texts_to_embed)
        # ^ Translates combined string batch into mathematical coordinate vectors
        
        points = []
        # ^ Initializes empty list to collect compiled Qdrant PointStruct points
        for idx, row in enumerate(batch):
        # ^ Loops through row batch indices and values
            f_id, f_url, f_question, f_answer = row
            # ^ Unpacks ID, URL, question, and answer fields
            points.append(
            # ^ Appends structured search point to batch list
                models.PointStruct(
                # ^ Instantiates Qdrant PointStruct builder class
                    id=f_id,
                    # ^ Enforces unique database ID keys
                    vector=embeddings[idx].tolist(),
                    # ^ Converts numpy float coordinates list array into python floats list
                    payload={
                    # ^ Appends payload context dictionary mapped to the vector coordinate
                        "id": f_id,
                        # ^ Unique database record ID integer
                        "url": f_url,
                        # ^ citation URL link string
                        "source": "IslamQA.info",
                        # ^ Source website publisher label
                        "question": f_question,
                        # ^ Text of user-submitted question payload string
                        "answer" : f_answer  
                        # ^ Full answer text chunk string
                    }
                    # ^ Ends payload mapping
                )
                # ^ Ends PointStruct instantiation
            )
            # ^ Ends points append
        
        print(f"    [*] Upserting batch {i//batch_size + 1} to Qdrant...")
        # ^ Outputs chunk status updates to console log
        client.upsert(collection_name=COLLECTION_NAME, points=points)
        # ^ Upserts points batch directly to active Qdrant vector database collection
    # ^ Ends batch ingestion loop

    print(f"\n[SUCCESS] Ingestion complete. {len(rows)} fatwas are now in Qdrant!")
    # ^ Prints success summary message to console log
    conn.close()
    # ^ Closes database connection to clean up memory resources
    return client, model
    # ^ Returns initialized client and model objects back to testing functions
# ^ Ends ingest function

# 🏛️ CONCEPT: Semantic searches verify ingestion databases by mapping string queries into coordinate vectors.
#    Nearest-neighbor lookups fetch matching payloads sorted by coordinate proximity.
# 🏛️ ANALOGY: test_qdrant_search is like checking if a directory search tool actually finds files.
#    We enter a key word (search query), translate it, check the catalog indexes, and print out the books found.
def test_qdrant_search(client, model, test_query):
# ^ Declares test search diagnostic function accepting clients, models, and query string
    print("\n" + "="*50)
    # ^ Draws border header line separator
    print(f"[*] RUNNING TEST QUERY: '{test_query}'")
    # ^ Prints test query description to console log
    print("="*50)
    # ^ Draws border header line separator

    query_vector = model.encode(test_query).tolist()
    # ^ Translates text question into coordinates floats list
    search_response = client.query_points(
    # ^ Calls direct vector query search from Qdrant
        collection_name="islamqa",
        # ^ Target collection namespace
        query=query_vector,
        # ^ Supplies coordinate query vector array
        limit=3
        # ^ Limits search returns to top 3 matches
    )
    # ^ Ends query_points call
    search_results = search_response.points
    # ^ Gathers matching points list array

    if not search_results:
    # ^ Conditional check, validating if search response contains matches
        print("[-] No results found. (Is the database empty?)")
        # ^ Prints empty alert status to console log
        return
        # ^ Aborts function execution
    # ^ Ends search_results check

    for rank, result in enumerate(search_results, start=1):
    # ^ Loops through ranked matches
        print(f"Rank {rank} (Similarity Score: {result.score:.4f}):")
        # ^ Prints rank number and distance score metric to console
        print(f"  URL : {result.payload.get('url')}")
        # ^ Prints reference source URL from payload dictionary
        print(f"  Answer   : {result.payload.get('answer')}")
        # ^ Prints answer contents payload string to terminal console
        print("-" * 50)
        # ^ Draws line separator between result cards
    # ^ Ends loops
# ^ Ends test_qdrant_search function

if __name__ == "__main__":
# ^ Conditional block executing statements only when python script is run directly in terminal
    qdrant_client, ai_model = ingest()
    # ^ Triggers data ingestion and gets client/model reference links
    
    if qdrant_client and ai_model:
    # ^ Conditional check verifying that ingestion process completed successfully
        sample_question = "What are the conditions for accepting repentance?"
        # ^ Sets sample test query string
        test_qdrant_search(qdrant_client, ai_model, sample_question)
        # ^ Executes diagnostic test search lookup
    # ^ Ends checking block
# ^ Ends script execution block
