# File: backend/rag_pipeline.py
# Purpose: Implements the Retrieval-Augmented Generation (RAG) pipeline using LlamaIndex.
# Layer: Phase 3 — Knowledge Ingestion
# Depends on: llama-index, qdrant-client, ollama
# Created: 2026-06-03
# Developer: Shaik Hidayatullah

from llama_index.core import VectorStoreIndex, Settings
from llama_index.vector_stores.qdrant import QdrantVectorStore
from llama_index.embeddings.huggingface import HuggingFaceEmbedding
from llama_index.llms.ollama import Ollama
from openai import OpenAI
import json
from qdrant_client import QdrantClient
import os
# --- CONFIGURATION ---
# Assuming 'local_models' is in the root of your backend directory
# It's safer to use absolute paths to avoid working-directory issues in Docker
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
EMBED_MODEL_NAME = os.path.join(BASE_DIR, "local_models", "paraphrase-multilingual-MiniLM-L12-v2")

# We must use the SAME embedding model used during ingestion to ensure coordinates match.
#EMBED_MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
LLM_MODEL_NAME = "qwen3:4b"
COLLECTION_NAME = "islamqa"

class RagPipeline:
    def __init__(self):
        # 1. Initialize the Qdrant client (the "database connector").
        # Using localhost because FastAPI runs with 'network_mode: host'.
        self.client = QdrantClient(host="localhost", port=6333)
        
        # 2. Set up the Vector Store (the "storage shelf").
        # This tells LlamaIndex where to look for our fatwa coordinates.
        self.vector_store = QdrantVectorStore(
            client=self.client, 
            collection_name=COLLECTION_NAME,
            #with_payload=True
            text_key="answer"
        )
        
        # 3. Set up the Embedding Model (the "translator").
        # This converts user questions into coordinates so we can find matches in Qdrant.
        self.embed_model = HuggingFaceEmbedding(model_name=EMBED_MODEL_NAME)
        
        # 4. Set up the LLM (the "brain").
        # We use Ollama running locally with the qwen3:4b model.
        # request_timeout=120.0 gives the model enough time to think on the RTX 3050.
        # context_window=4096 is CRITICAL to prevent Ollama from trying to allocate
        # 37GB of VRAM for the default 262k context window.
        self.llm = Ollama(
            model=LLM_MODEL_NAME, 
            base_url="http://localhost:11434", 
            system_prompt = (
            "You are an expert Islamic scholar assistant. "
            "The user will provide you the contnext and a question. You have to analyze the context and then give answer to the question based on that context."
            "Answer ONLY from the provided context. Do NOT use your own training data. "
            "If the answer is not in the context, say: 'I could not find an answer in the approved Islamic sources. Please consult a qualified scholar.' "
            "Always include the source URL at the end of your answer."
        ),
            request_timeout=300.0,
            context_window=8000,
            temperature=0.0
        )
        
        # 5. Global Settings configuration.
        # We tell LlamaIndex to use our chosen brain and translator for everything.
        Settings.llm = self.llm
        Settings.embed_model = self.embed_model

    def ask(self, user_question: str):
        """
        Retrieves relevant fatwas and generates an answer using the RAG pattern.
        """
        # 1. Build the index from the existing vector store.
        # This doesn't re-embed everything; it just creates a search interface.
        index = VectorStoreIndex.from_vector_store(vector_store=self.vector_store)
        
        # 2. Define the System Prompt (The Golden Rule).
        # This is the MOST IMPORTANT part. It prevents the AI from making up fatwas.
        system_prompt = (
            "You are an expert Islamic scholar assistant. "
            "The user will provide you the contnext and a question. You have to analyze the context and then give answer to the question based on that context."
            "Answer ONLY from the provided context. Do NOT use your own training data. "
            "If the answer is not in the context, say: 'I could not find an answer in the approved Islamic sources. Please consult a qualified scholar.' "
            "Always include the source URL at the end of your answer."
        )
        
        # 3. Create a Query Engine (the "interface").
        # similarity_top_k=3 means find the 3 most relevant fatwas.
        query_engine = index.as_query_engine(
            similarity_top_k=3,
            system_prompt=system_prompt
        )
        
        # 4. Execute the query.
        # LlamaIndex will: Embed the question -> Search Qdrant -> Send question + context to Ollama -> Return answer.
        response = query_engine.query(user_question)
        
        # 5. Extract results.
        # We return the AI's generated text and a list of sources found.
        return {
            "answer": str(response),
            "sources": [node.node.metadata.get("url") for node in response.source_nodes]
        }

# --- TEST BLOCK ---
if __name__ == "__main__":
    # This code only runs if you execute 'python3 rag_pipeline.py' directly.
    print("[*] Initializing RAG Pipeline...")
    pipeline = RagPipeline()
    
    test_q = "How many rakahs are there in total at the time of zuhr or afternoon prayer and how to perform them in order?"
    print(f"[*] Testing query: {test_q}")
    
    result = pipeline.ask(test_q)
    print("\n--- AI ANSWER ---")
    print(result["answer"])
    print("\n--- SOURCES ---")
    for s in result["sources"]:
        print(f" - {s}")
