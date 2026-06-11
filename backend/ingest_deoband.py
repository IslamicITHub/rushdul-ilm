# File: backend/ingest_deoband.py
# Purpose: Converts Deoband SQLite fatwas into vector embeddings and saves them in Qdrant.
# Layer: Phase 3 — Knowledge Ingestion
# Depends on: sqlite3, qdrant_client, sentence_transformers
# Created: 2026-06-06 | Modified: 2026-06-11
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
SQLITE_DB_PATH = "../darulifta-deoband.com-offline/offline_darulifta2/offline.sqlite"
# ^ Filepath pointing to local SQLite database containing offline Hanafi fatwa scraping data
QDRANT_HOST = "localhost"
# ^ Default network address name mapping connection routes to the local Qdrant server
QDRANT_PORT = 6333
# ^ The port address where Qdrant database listens for incoming client requests
COLLECTION_NAME = "deoband"
# ^ Target collection namespace inside Qdrant database for Deoband fatwas

# 🏛️ CONCEPT: Knowledge ingestion pipelines convert unstructured text documents into vector databases.
#    This process loops through SQLite database records, generates vector arrays, and upserts them to collections.
# 🏛️ ANALOGY: Ingesting Deoband fatwas is like moving physical fatwa archives into a cataloged library.
#    We read the title/text (Fetch), write down the catalog number (Embeddings), and place it on the library shelf (Qdrant).
def ingest():
# ^ Declares ingest function orchestrating data indexing operations
    print(f"[*] Connecting to Qdrant at {QDRANT_HOST}:{QDRANT_PORT}...")
    # ^ Outputs status update to console log
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)
    # ^ Establishes client driver connection targeting Qdrant server port

    print(f"[*] Loading embedding model: {MODEL_NAME}...")
    # ^ Outputs status update to console log
    model = SentenceTransformer(MODEL_NAME, device="cuda")
    # ^ Instantiates embedding translator model running on CUDA GPU for faster ingestion processing

    print(f"[*] Setting up Qdrant collection: {COLLECTION_NAME} (384 dim)...")
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
        return
        # ^ Aborts function execution
    # ^ Ends path validation

    conn = sqlite3.connect(SQLITE_DB_PATH)
    # ^ Opens SQLite connection database reference
    cursor = conn.cursor()
    # ^ Instantiates cursor engine to execute database SQL statements

    print("[*] Fetching fatwas from SQLite...")
    # ^ Outputs status update to console log
    cursor.execute("SELECT url, answer FROM fatwas")
    # ^ Executes SQL query selecting URL links and answers text columns
    rows = cursor.fetchall()
    # ^ Gathers all matching records from SQLite database into memory
    print(f"[+] Found {len(rows)} fatwas in Deoband database.")
    # ^ Prints count of fetched database rows to console log

    batch_size = 6
    # ^ Sets batch size processing count to limit RAM spikes during embedding generation
    for i in range(0, len(rows), batch_size):
    # ^ Loops through the rows in chunks of batch_size steps
        batch = rows[i : i + batch_size]
        # ^ Extracts current chunk subset slice of rows
        texts_to_embed = [f"{row[0] or ''} {row[1] or ''}" for row in batch]
        # ^ Combines URL and answer text strings, substituting None values safely
        
        print(f"    [*] Generating embeddings for batch {i//batch_size + 1}...")
        # ^ Outputs chunk status updates to console log
        embeddings = model.encode(texts_to_embed)
        # ^ Translates combined string batch into mathematical coordinate vectors
        
        points = []
        # ^ Initializes empty list to collect compiled Qdrant PointStruct points
        for idx, row in enumerate(batch):
        # ^ Loops through row batch indices and values
            f_url, f_question_and_answer = row
            # ^ Unpacks URL and question/answer fields
            question_and_answer_text = f_question_and_answer or ""
            # ^ Handles None values safely
            points.append(
            # ^ Appends structured search point to batch list
                models.PointStruct(
                # ^ Instantiates Qdrant PointStruct builder class
                    id=i + idx,
                    # ^ Enforces simple incremental integer ID keys
                    vector=embeddings[idx].tolist(),
                    # ^ Converts numpy float coordinates list array into python floats list
                    payload={
                    # ^ Appends payload context dictionary mapped to the vector coordinate
                        "url": f_url,
                        # ^ citation URL link string
                        "source": "Darul Ifta Deoband (Hanafi)",
                        # ^ Source website publisher label
                        "question_and_answer": question_and_answer_text
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

    print(f"\n[SUCCESS] Deoband Ingestion complete. {len(rows)} fatwas are now in Qdrant (384 dim)!")
    # ^ Prints success summary message to console log
    conn.close()
    # ^ Closes database connection to clean up memory resources
# ^ Ends ingest function

# 🏛️ CONCEPT: Semantic searches verify ingestion databases by mapping string queries into coordinate vectors.
#    Nearest-neighbor lookups fetch matching payloads sorted by coordinate proximity.
# 🏛️ ANALOGY: Test search is like testing a key on a newly installed gate.
#    We translate the text key (query), check if it fits the locks (distance), and check if the gate opens to the correct lawn.
def test_search():
# ^ Declares test search diagnostic function
    client = QdrantClient(host="localhost", port=6333)
    # ^ Instantiates Qdrant client connection pointing to port 6333
    
    model = SentenceTransformer(MODEL_NAME)
    # ^ Loads active embedding transformer model
    
    query_text = "What breaks or invalidates the fast in ramadan?"
    # ^ Defines test question string
    
    print(f"[*] Translating query into numbers: '{query_text}'")
    # ^ Prints status update to console log
    query_vector = model.encode(query_text).tolist()
    # ^ Translates text question into coordinates floats list
    
    print("[*] Searching the 'Deoband' collection...")
    # ^ Prints search status notification to console log
    search_response = client.query_points(
    # ^ Calls direct vector query search from Qdrant
        collection_name=COLLECTION_NAME,
        # ^ Target collection namespace
        query=query_vector,
        # ^ Supplies coordinate query vector array
        limit=10
        # ^ Limits search returns to top 10 matches
    )
    # ^ Ends query_points call
    search_results = search_response.points
    # ^ Extracts point results list array
    print("\n" + "="*60)
    # ^ Draws border header line separator
    print("MATCHING RESULTS FOUND IN QDRANT DB:")
    # ^ Prints output title section header
    print("="*60)
    # ^ Draws border header line separator
    
    for rank, hit in enumerate(search_results, start=1):
    # ^ Loops through ranked matches
        print(f"Rank {rank} (Confidence Score: {hit.score:.4f}):")
        # ^ Prints rank number and distance score metric to console
        print(f"  URL   : {hit.payload.get('url')}")
        # ^ Prints reference source URL from payload dictionary
        print(f"  Answer  : {hit.payload.get('question_and_answer')}...")
        # ^ Prints answer contents payload string to terminal console
        print("-" * 60)
        # ^ Draws line separator between result cards
    # ^ Ends loops
# ^ Ends test_search function

if __name__ == "__main__":
# ^ Conditional block executing statements only when python script is run directly in terminal
    test_search()
    # ^ Triggers diagnostic test search execution
# ^ Ends script execution block
