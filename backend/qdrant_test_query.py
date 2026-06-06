"""
# File: backend/ingest_islamqa.py
# Purpose: Converts IslamQA SQLite fatwas into vector embeddings and saves them in Qdrant.
# Layer: Phase 3 — Knowledge Ingestion
# Created: 2026-06-03 | Modified: 2026-06-03
# Developer: Shaik Hidayatullah

===============================================================================
BEGINNER'S GUIDE TO THE CONCEPTS USED IN THIS SCRIPT
===============================================================================

1. EMBEDDINGS:
   Computers don't understand words; they only understand numbers. An "embedding" 
   is a way to convert text (like a sentence or a paragraph) into a long list of 
   numbers (called a "vector"). The magic is that sentences with similar meanings 
   will have similar numbers. This allows the computer to understand "semantic 
   meaning" rather than just looking for exact keyword matches.

2. VECTOR DATABASE (Vector DB):
   A traditional database (like SQLite) stores data in rows and columns and is 
   great for exact matches (e.g., "Find user where ID = 5"). A Vector DB is 
   designed to store embeddings (those long lists of numbers) and perform 
   "similarity searches." It allows you to ask: "Find the vectors in the database 
   that are mathematically closest to the vector of my search query."

3. QDRANT:
   Qdrant is a specific, highly efficient open-source Vector Database. It stores 
   our vectors (embeddings) alongside "payloads" (metadata like titles and URLs) 
   so we can quickly retrieve the original text when we find a mathematical match.

4. COSINE SIMILARITY:
   This is the mathematical formula Qdrant uses to determine how close two vectors 
   are to each other. It measures the angle between two lines (vectors) in a 
   multi-dimensional space. A smaller angle means the texts are more similar in meaning.
===============================================================================
"""

# Import the sqlite3 library to interact with our SQLite database file
import sqlite3
# Import the os library to check if files exist on our computer's operating system
import os

# Import Qdrant client to communicate with the Qdrant database server
from qdrant_client import QdrantClient
# Import models from Qdrant to define data structures (like Vectors and Points)
from qdrant_client.http import models
# Import SentenceTransformer to load the AI model that creates our text embeddings
from sentence_transformers import SentenceTransformer

# --- CONFIGURATION ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_NAME = os.path.join(BASE_DIR, "local_models", "paraphrase-multilingual-MiniLM-L12-v2")

# We use 'paraphrase-multilingual-MiniLM-L12-v2' (420MB) instead of MPNET (1.1GB)
# to avoid download timeouts, while still supporting Telugu and Urdu.
#MODEL_NAME = 'paraphrase-multilingual-MiniLM-L12-v2'

# The size of the vector depends entirely on the AI model used. 
# Our chosen MiniLM model outputs exactly 384 numbers per sentence.
VEC_SIZE = 384

# The file path where our traditional SQLite database is stored
SQLITE_DB_PATH = "/data/islamqa/islamqa_database.sqlite"

# The address and port where the Qdrant server is running
QDRANT_HOST = "localhost"
QDRANT_PORT = 6333
COLLECTION_NAME = "islamqa"


def ingest():
    """
    Reads data from SQLite, converts text to embeddings, and uploads to Qdrant.
    Returns the initialized Qdrant client and AI model so we can use them for testing later.
    """
    print(f"[*] Connecting to Qdrant at {QDRANT_HOST}:{QDRANT_PORT}...")
    # Create a connection object to talk to the Qdrant server
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)

    print(f"[*] Loading embedding model: {MODEL_NAME}...")
    # Load the AI model into memory. (It will download from the internet on the first run)
    model = SentenceTransformer(MODEL_NAME)

    print(f"[*] Setting up Qdrant collection: {COLLECTION_NAME}...")
    # A "collection" in Qdrant is like a "table" in SQL. 
    # 'recreate_collection' will delete the collection if it already exists and make a fresh one.
    client.recreate_collection(
        collection_name=COLLECTION_NAME,
        # We must tell Qdrant how big our vectors are (384) and how to compare them (Cosine)
        vectors_config=models.VectorParams(size=VEC_SIZE, distance=models.Distance.COSINE),
    )

    # Check if the SQLite file actually exists before we try to open it
    if not os.path.exists(SQLITE_DB_PATH):
        print(f"[!] Error: SQLite database not found at {SQLITE_DB_PATH}")
        # Return None to indicate failure
        return None, None

    # Connect to the SQLite database
    conn = sqlite3.connect(SQLITE_DB_PATH)
    # Create a cursor object, which is used to execute SQL commands
    cursor = conn.cursor()

    print("[*] Fetching fatwas from SQLite...")
    # Execute an SQL query to get specific columns from the 'fatwas' table
    #cursor.execute("SELECT id, title, question, url FROM fatwas")
    cursor.execute("SELECT id, url, question, answer FROM fatwas")
    # Fetch all the results of the query and store them in a list called 'rows'
    rows = cursor.fetchall()
    print(f"[+] Found {len(rows)} fatwas.")

    # We process the data in small chunks (batches) so we don't run out of computer memory
    batch_size = 100
    
    # Loop through the list of rows, jumping by 'batch_size' (0, 100, 200, etc.)
    for i in range(0, len(rows), batch_size):
        # Slice the list to get just the current batch of rows
        batch = rows[i : i + batch_size]
        
        # Combine the question (row[2]) and answer (row[3]) into a single string for the AI to read
        texts_to_embed = [f"{row[2]} {row[3]}" for row in batch]
        
        print(f"    [*] Generating embeddings for batch {i//batch_size + 1}...")
        # Use the AI model to convert our list of text strings into a list of number vectors
        embeddings = model.encode(texts_to_embed)
        
        # Create an empty list to hold the formatted data points for Qdrant
        points = []
        
        # Loop through each item in our current batch, getting both its index number (idx) and data (row)
        for idx, row in enumerate(batch):
            # Unpack the 4 columns from our SQL query into 4 variables
            f_id, f_url, f_question, f_answer = row
            
            # Construct a "PointStruct", which is Qdrant's required format for saving data
            points.append(
                models.PointStruct(
                    id=f_id,                            # The unique ID from SQLite
                    vector=embeddings[idx].tolist(),    # The mathematical vector (converted to a standard Python list)
                    payload={                           # Payload is extra info attached to the vector
                        "id": f_id,
                        "url": f_url,
                        "source": "IslamQA.info",
                        "question": f_question[:2000], # Store the text so we can read it later (capped at 2000 chars to save space)
                        "answer" : f_answer  
                    }
                )
            )
        
        print(f"    [*] Upserting batch {i//batch_size + 1} to Qdrant...")
        # Upload the list of points to the Qdrant database. ("Upsert" means Update or Insert)
        client.upsert(collection_name=COLLECTION_NAME, points=points)

    print(f"\n[SUCCESS] Ingestion complete. {len(rows)} fatwas are now in Qdrant!")
    # Always close the database connection when you are done
    conn.close()
    
    # Return the client and model so we can use them in the test function below
    return client, model


def test_qdrant_search(client, model, test_query):
    """
    Takes a plain text query, converts it to an embedding, and asks Qdrant 
    to find the most similar fatwas in the database.
    """
    print("\n" + "="*50)
    print(f"[*] RUNNING TEST QUERY: '{test_query}'")
    print("="*50)

    # Step 1: Convert the user's text question into a mathematical vector
    # using the exact same AI model we used during ingestion.
    query_vector = model.encode(test_query).tolist()

    # Step 2: Search Qdrant for the closest matching vectors
    search_results = client.search(
        collection_name=COLLECTION_NAME,
        query_vector=query_vector,
        limit=3  # Ask Qdrant to return the top 3 closest matches
    )

    # Step 3: Print out the results so we can see them
    if not search_results:
        print("[-] No results found. (Is the database empty?)")
        return

    for rank, result in enumerate(search_results, start=1):
        # result.score shows how similar the match is mathematically
        # result.payload contains the title, url, and text we saved earlier
        print(f"Rank {rank} (Similarity Score: {result.score:.4f}):")
        print(f"  URL : {result.payload.get('url')}")
        print(f"  Answer   : {result.payload.get('answer')}")
        #print(f"  Snippet: {result.payload.get('text')[:150]}...") # Print first 150 chars of the text
        print("-" * 50)


# This is the standard entry point for a Python script.
# It ensures the code inside only runs if the script is executed directly 
# (not if it is imported into another file).
if __name__ == "__main__":
    # 1. Run the ingestion process
    qdrant_client, ai_model = ingest()
    
    # 2. If ingestion was successful (it didn't return None), run a test query
    if qdrant_client and ai_model:
        # You can change this string to test different searches based on the IslamQA data
        sample_question = "What are the conditions for accepting repentance?"
        test_qdrant_search(qdrant_client, ai_model, sample_question)
