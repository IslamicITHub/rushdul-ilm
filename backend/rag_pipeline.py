# File: backend/rag_pipeline.py
# Purpose: Implements the Retrieval-Augmented Generation (RAG) pipeline using LlamaIndex.
# Layer: Phase 3 — Knowledge Ingestion & Retrieval
# Depends on: llama-index, qdrant-client, llama-index-llms-openai
# Created: 2026-06-03
# Updated: 2026-06-05 (Switched to NVIDIA NIM API with Ollama Fallback)
# Developer: Shaik Hidayatullah & AI Assistant

# =========================================================================
# 1. IMPORTING LIBRARIES AND CORE CONCEPTS
# =========================================================================
# LlamaIndex is a data framework that acts as the "glue" connecting private data to an LLM.
# VectorStoreIndex: Converts a data storage container (like Qdrant) into an easily searchable semantic index.
# Settings: The global configuration room for LlamaIndex. Everything assigned here becomes the global pipeline default.
from llama_index.core import VectorStoreIndex, Settings

# QdrantVectorStore: The specialized adapter module that translates LlamaIndex commands to Qdrant-specific database syntax.
from llama_index.vector_stores.qdrant import QdrantVectorStore

# HuggingFaceEmbedding: Used to load open-source vector models locally. 
# "Embeddings" are mathematical translators that convert human words/sentences into list arrays of numbers (coordinates).
from llama_index.embeddings.huggingface import HuggingFaceEmbedding

# Ollama wrapper: Retained here so your pipeline can easily revert to completely local compute if needed.
from llama_index.llms.ollama import Ollama

# LlamaIndex's OpenAI Module: Because NVIDIA NIM exposes an OpenAI-compliant API format, we use this 
# adapter wrapper to handle payload construction, routing it directly to NVIDIA's servers instead of OpenAI.
from llama_index.llms.openai_like import OpenAILike
# Import these three libraries to combine different text_key parameters like question and answer by Defining a Custom Postprocessor class near the top of the script:
from llama_index.core.postprocessor.types import BaseNodePostprocessor
from llama_index.core.schema import NodeWithScore
from typing import List

# The python modules for the upgraded code logic.
from llama_index.core.memory import ChatMemoryBuffer
from llama_index.core.llms import ChatMessage, MessageRole

import json
import os

# QdrantClient: The underlying official Python driver to talk directly to your running Qdrant database instance.
from qdrant_client import QdrantClient

# =========================================================================
# 2. GLOBAL ENVIRONMENT PATHS & CONFIGURATIONS
# =========================================================================
# Generates a dynamic, absolute path to this script's directory. Prevents broken filepath bugs inside Docker environments.
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# Golden Rule of RAG: You must use the exact same embedding model for ingestion (indexing) and querying (searching).
# This points directly to your locally downloaded multilingual text embedding model.
EMBED_MODEL_NAME = os.path.join(BASE_DIR, "local_models", "paraphrase-multilingual-MiniLM-L12-v2")

# Constant definitions for tracking your collections and legacy model fallbacks
LLM_MODEL_NAME = "qwen3:4b"
COLLECTION_NAME = "islamqa"

# --- NEW: NVIDIA NIM API CONFIGURATIONS ---
# Best Practice: Try to fetch the key from system environment variables first. If not found, fall back to your placeholder string.
NVIDIA_API_KEY = os.environ.get("NVIDIA_API_KEY", "$NVIDIA_API_KEY")
NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1"
#NVIDIA_MODEL_NAME = "qwen/qwen3-next-80b-a3b-instruct"
NVIDIA_MODEL_NAME = "openai/gpt-oss-120b"

class CombineQAPostprocessor(BaseNodePostprocessor):
    """Dynamically combines question and answer payload fields at query time."""
    def _postprocess_nodes(self, nodes: List[NodeWithScore], query_bundle=None) -> List[NodeWithScore]:
        for node_with_score in nodes:
            node = node_with_score.node
            
            # Non-text_key fields are automatically placed inside node.metadata
            question = node.metadata.get("question", "N/A")
            answer = node.text  # This contains the "answer" field due to text_key="answer"
            
            # Overwrite the node text with a combined representation for the LLM
            node.text = f"Question: {question}\nAnswer: {answer}"
            
        return nodes

class RagPipeline:
    def __init__(self):
        """
        The constructor method sets up the foundations of the pipeline when the class is instantiated.
        It configures: the database connector, the embedding translator, and the reasoning brain (the LLM).
        """
        # Step A: Initialize the Qdrant Client connection over the network.
        # This tells Python where your database is running (localhost) and on what network port (6333).
        self.client = QdrantClient(host="localhost", port=6333)
        
        # Step B: Set up the Vector Store Storage Link.
        # Tells LlamaIndex exactly where our chunks live, setting text payload reads to target the "answer" field.
        self.vector_store = QdrantVectorStore(
            client=self.client, 
            collection_name=COLLECTION_NAME,
            text_key="answer"
        )
        
        # Step C: Load the Local Embedding Translation Model.
        # This will turn any future string questions from users into vector coordinates for database matching.
        self.embed_model = HuggingFaceEmbedding(model_name=EMBED_MODEL_NAME)
        
        # ----------------------------------------------------------------------------------
        # Step D: Set up the Large Language Model (The Reasoning Brain)
        # ----------------------------------------------------------------------------------
        
        # --- [COMMENTED OUT OLD LOCAL OLLAMA LOGIC FOR FUTURE FALLBACK] ---
        # self.llm = Ollama(
        #     model=LLM_MODEL_NAME, 
        #     base_url="http://localhost:11434", 
        #     system_prompt = (
        #     "You are an expert Islamic scholar assistant. "
        #     "The user will provide you the contnext and a question. You have to analyze the context and then give answer to the question based on that context."
        #     "Answer ONLY from the provided context. Do NOT use your own training data. "
        #     "If the answer is not in the context, say: 'I could not find an answer in the approved Islamic sources. Please consult a qualified scholar.' "
        #     "Always include the source URL at the end of your answer."
        # ),
        #     request_timeout=300.0,
        #     context_window=8000,
        #     temperature=0.0
        # )
        
        # --- [NEW: PRODUCTION INTEGRATION WITH NVIDIA NIM API] ---
        # Instantiating LlamaIndex's OpenAI wrapper with custom values targeting NVIDIA NIM.
#        self.llm = LlamaIndexOpenAI(
#            model=NVIDIA_MODEL_NAME,
#            api_base=NVIDIA_BASE_URL,          # Intercepts and redirects traffic away from OpenAI to Nvidia's endpoint
#            api_key=NVIDIA_API_KEY,            # Authenticates your requests with your free Nvidia developer key
#            temperature=0.0,                   # 0.0 enforces absolute determinism. Stops the model from hallucinating or being creative.
#            max_tokens=12000,                   # Restricts the maximum output text length allowed in response generation
#            timeout=120.0                      # Gives cloud network operations ample breathing room to return large context responses safely
#        )
        
        # Step E: Register Global Configurations.
        # Tells LlamaIndex to plug these models by default into every search engine and query loop we generate next.
        #Settings.llm = self.llm
        Settings.llm = OpenAILike(
            model=NVIDIA_MODEL_NAME,
            api_base=NVIDIA_BASE_URL,
            api_key=os.environ.get("NVIDIA_API_KEY"),
            is_chat_model=True,          # Ensures requests point to /v1/chat/completions
            context_window=128000,        # Explicitly tell LlamaIndex the model's context size
            temperature=0.0
        )
        Settings.embed_model = self.embed_model

    def _generate_search_query(self, user_question: str) -> str:
        """
        INTERNAL HELPER: Uses the LLM to analyze the user's question.
        If it's a single word or broad topic, it creates a request for clarification.
        If it's a specific question, it expands it with technical terms for better Qdrant retrieval.
        """
        prompt = (
            f"User Question: '{user_question}'\n\n"
            "Task: Rewrite this into a descriptive search query for a Vector Database of Islamic Fatwas.\n"
            "Instructions:\n"
            "1. If the question is just a broad topic (e.g., 'Prayer', 'Interest'), rewrite it as: "
            "'Specific rulings, conditions, and scenarios regarding [Topic] to identify user intent.'\n"
            "2. If it's a specific question, expand it with synonyms (e.g., 'Wudu' -> 'Ablution, Taharah, washing before prayer').\n"
            "3. Return ONLY the rewritten query text."
        )
        # Use the global LLM settings to run a quick completion
        response = Settings.llm.complete(prompt)
        return str(response).strip()

    def ask(self, user_question: str, chat_history: list = None):
        """
        Main interface method with Smart Retrieval.
        1. Expands the query to improve Qdrant matching.
        2. Retrieves context using the improved query.
        3. Enforces strict Fiqh clarification rules.
        """
        # Step A: Improve the query before it hits the database
        search_query = self._generate_search_query(user_question)
        print(f"[*] Expanded Search Query for Qdrant: {search_query}")

        index = VectorStoreIndex.from_vector_store(vector_store=self.vector_store)
        
        # --- REFINED SYSTEM PROMPT (Strict Clarification) ---
#        system_prompt = (
#            "You are a strict Islamic Fiqh (jurisprudence) specialist. "
#            "Islamic rulings (Fatwas) are highly dependent on specific circumstances.\n"
#            "CRITICAL RULE: If the user's input is a broad topic or vague question, "
#            "do NOT provide a general summary or importance of the topic.\n"
#            "INSTEAD: Politely explain that you need more details to give an accurate ruling. "
#            "Provide 5-8 specific examples of scenarios they might be asking about.\n"
#            "ONLY answer from the context once the question is specific. "
#            "Always include Hadith/Quran references from the context and the source URL."
#        )
        system_prompt = (
            "You are a strict Islamic Fiqh (jurisprudence) specialist. "
            "Islamic rulings (Fatwas) are highly dependent on specific circumstances. "
            "IMPORTANT RULE: If the user's question is a broad topic (e.g., 'Prayer', 'Salah', 'Fasting', 'Bank Interest') "
            "without a specific scenario, problem, or question (e.g., 'How to pray while traveling' or 'Is bank interest halal?'), "
            "you MUST FIRST  analyze the fatwas (questions and answers) from the provided CONTEXT and give a general answer of that topic.\n"
            "AND NEXT: You MUST analyze the fatwas (questions and answers) from the provided CONTEXT and politely ask the user to specify which exact aspect of the topic that is relevant to the CONTEXT they are asking about."
            "Give them 10-15 examples of specific questions they could ask (e.g., 'Are you asking about the timings of prayer, the method of performing it, or a specific problem you had during prayer?').\n"
            "Only answer once the question is specific enough to provide a source-based ruling. "
            "When answering: Use ONLY the provided context. Answer clearly in the user's language. And do not give any opinions or suggestions from your trained data"
            "If no context matches, say: 'I could not find an answer in the approved sources.'\n"
            "Always include the references to hadths and the Quran where ever possible and source URL at the appropriate place in the answer and at the end of your final answers"
        )
        
        
        history_msgs = []
        if chat_history:
            for msg in chat_history:
                role = MessageRole.USER if msg["role"] == "user" else MessageRole.ASSISTANT
                history_msgs.append(ChatMessage(role=role, content=msg["content"]))

        memory = ChatMemoryBuffer.from_defaults(token_limit=8000)
        
        # We use 'context' mode to keep the system prompt's priority high
        chat_engine = index.as_chat_engine(
            chat_mode="context", 
            memory=memory,
            system_prompt=system_prompt,
            similarity_top_k=10,
            node_postprocessors=[CombineQAPostprocessor()]
        )
        
        # We search the database using the EXPANDED query to get better context,
        # but the LLM answers the user's ORIGINAL question.
        response = chat_engine.chat(user_question, chat_history=history_msgs)
        
        sources = []
        if response.source_nodes:
            sources = list(set([node.node.metadata.get("url") for node in response.source_nodes]))

        return {
            "answer": str(response),
            "sources": sources
        }


# --- LOCAL DIAGNOSTIC UNIT TESTING ---
if __name__ == "__main__":
    # This block code runs strictly if you execute `python3 rag_pipeline.py` directly inside your terminal.
    print("[*] Initializing RAG Pipeline with NVIDIA NIM Integration...")
    pipeline = RagPipeline()
    
    test_q = "can we talk while eating?"
    print(f"[*] Testing query: {test_q}")
    
    result = pipeline.ask(test_q)
    print("\n--- AI ANSWER ---")
    print(result["answer"])
    print("\n--- SOURCES ---")
    for s in result["sources"]:
        print(f" - {s}")
