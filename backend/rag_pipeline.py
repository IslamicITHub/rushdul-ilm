# File: backend/rag_pipeline.py
# Purpose: Implements the Retrieval-Augmented Generation (RAG) pipeline using LlamaIndex.
# Layer: Phase 3 — Knowledge Ingestion & Retrieval
# Depends on: llama-index, qdrant-client, llama-index-llms-openai
# Created: 2026-06-03 | Modified: 2026-06-11
# Developer: Shaik Hidayatullah & AI Assistant

from llama_index.core import VectorStoreIndex, Settings
# ^ VectorStoreIndex converts vector databases into index schemas; Settings controls global pipeline configurations
from llama_index.vector_stores.qdrant import QdrantVectorStore
# ^ QdrantVectorStore acts as the adapter module connecting LlamaIndex commands to Qdrant storage systems
from llama_index.embeddings.huggingface import HuggingFaceEmbedding
# ^ HuggingFaceEmbedding compiles vector embeddings locally using local open-source transformer models
from llama_index.llms.ollama import Ollama
# ^ Ollama wrapper connects LlamaIndex to our locally running Ollama LLM endpoint fallback service
from llama_index.llms.openai_like import OpenAILike
# ^ OpenAILike translates payload structures to interface with NVIDIA's OpenAI-compatible NIM APIs
from llama_index.core.postprocessor.types import BaseNodePostprocessor
# ^ BaseNodePostprocessor serves as the base class for custom node post-filtering and formatting actions
from llama_index.core.schema import NodeWithScore, TextNode
# ^ NodeWithScore structures retrieved vectors alongside search score ratings; TextNode defines basic text units
from llama_index.core.retrievers import BaseRetriever
# ^ BaseRetriever serves as the foundational abstract class for custom search query retriever classes
from typing import List
# ^ List imports type hints for lists containing structured items (like List[str])
from llama_index.core.memory import ChatMemoryBuffer
# ^ ChatMemoryBuffer manages conversation token buffer memory windows to preserve conversation context
from llama_index.core.llms import ChatMessage, MessageRole
# ^ ChatMessage models conversation messages; MessageRole identifies roles like USER or ASSISTANT
import json
# ^ Standard python library to parse and format JSON string objects
import os
# ^ Standard python library to interact with system environments, folders, and absolute file paths
from qdrant_client import QdrantClient
# ^ Client class driving network requests directly to self-hosted Qdrant server collections

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# ^ Extracts the absolute directory path of this file to prevent filepath bugs inside Docker containers
EMBED_MODEL_NAME = os.path.join(BASE_DIR, "local_models", "qwen3_embedding_06b_local")
# ^ Chains file paths to target the locally downloaded Qwen multilingual embedding folder
LLM_MODEL_NAME = "qwen3:4b"
# ^ Standard model tag representing our local Qwen3 4-billion parameter LLM fallback model
COLLECTION_NAME = "deoband"
# ^ Default collection namespace used when searching the Qdrant database
QDRANT_HOST = "localhost"
# ^ Default network address name mapping connection routes to the local Qdrant server
NVIDIA_API_KEY = os.environ.get("NVIDIA_API_KEY", "$NVIDIA_API_KEY")
# ^ Fetches Nvidia developer credentials key from active system environment variables
NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1"
# ^ Cloud gateway network address routing requests to NVIDIA NIM acceleration endpoint services
NVIDIA_MODEL_NAME = "openai/gpt-oss-20b"
# ^ Target NVIDIA NIM cloud LLM model name representing the primary 70-billion parameter model

# 🏛️ CONCEPT: Custom post-processors modify retrieved text chunks before passing them to the LLM.
#    This allows combining metadata payload fields (like separate question/answer fields) into a single text block.
# 🏛️ ANALOGY: CombineQAPostprocessor is like a compiler assistant at a news agency.
#    It takes the reporter's raw question notes and draft answer notes from different drawers,
#    glues them onto a single sheet of paper, and hands that single sheet to the editor-in-chief (LLM).
class CombineQAPostprocessor(BaseNodePostprocessor):
# ^ Declares class CombineQAPostprocessor inheriting from LlamaIndex's BaseNodePostprocessor
    def _postprocess_nodes(self, nodes: List[NodeWithScore], query_bundle=None) -> List[NodeWithScore]:
    # ^ Internal abstract method overrides that process retrieved Node lists and return formatted Node lists
        for node_with_score in nodes:
        # ^ Loops through every retrieved node container wrapper in the list
            node = node_with_score.node
            # ^ Extracts the underlying TextNode data object from wrapper container
            
            question = node.metadata.get("question", "N/A")
            # ^ Safely fetches original fatwa question string from metadata dictionary
            answer = node.text
            # ^ Fetches raw answer text chunk (stored inside node text by database schema)
            
            node.text = f"Question: {question}\nAnswer: {answer}"
            # ^ Merges question and answer segments into a single string, replacing node text payload
            
        return nodes
        # ^ Returns the processed node list to the caller
# ^ Ends CombineQAPostprocessor class definition

# 🏛️ CONCEPT: Custom Retrievers bypass standard single-collection constraints in LlamaIndex.
#    They run parallel queries across multiple vector collections (like islamqa and deoband) and merge results.
# 🏛️ ANALOGY: MultiCollectionRetriever is like a research librarian with keys to multiple archive rooms.
#    Instead of just searching the Hanafi section, they copy the query, run searches in both the Hanafi and general rooms,
#    gather the top books from all searches, sort them by relevance, and hand back the best collection.
class MultiCollectionRetriever(BaseRetriever):
# ^ Declares class MultiCollectionRetriever inheriting from LlamaIndex's BaseRetriever
    def __init__(self, client: QdrantClient, collections: List[str], embed_model, similarity_top_k: int = 10, search_queries: List[str] = None, enable_reranking: bool = False):
    # ^ Constructor initializing client, collections list, embedding model, and search result limits
        self.client = client
        # ^ Binds the active Qdrant client connection driver instance to self
        self.collections = collections
        # ^ Binds the target list of collection names (e.g. ['islamqa', 'deoband']) to self
        self.embed_model = embed_model
        # ^ Binds the active embedding translator model instance to self
        self.similarity_top_k = similarity_top_k
        # ^ Binds default search return size count parameter to self
        self.search_queries = search_queries
        # ^ Binds multi-query search strings
        self.enable_reranking = enable_reranking
        # ^ Flag to enable CrossEncoder reranking
        
        # Initialize CrossEncoder if reranking is enabled
        self.reranker = None
        if self.enable_reranking:
            try:
                from sentence_transformers import CrossEncoder
                print("[*] Loading CrossEncoder for Reranking...")
                self.reranker = CrossEncoder("cross-encoder/ms-marco-TinyBERT-L-2-v2", max_length=512)
            except Exception as e:
                print(f"[!] Failed to load CrossEncoder: {e}")
                self.enable_reranking = False

        super().__init__()
        # ^ Triggers base retriever constructor to initialize internals properly
# ^ Ends constructor block

    def _retrieve(self, query_bundle, **kwargs) -> List[NodeWithScore]:
    # ^ Internal abstract method implementation executing search operations and returning list of NodeWithScore
        queries_to_run = self.search_queries if self.search_queries else [query_bundle.query_str]
        
        print(f"\n[DEBUG RAG] Starting vector retrieval using {len(queries_to_run)} query variations.")
        # ^ Debug log indicating how many queries are about to be fired against the database
        
        all_points_dict = {}
        # ^ Initializes a dict to gather unique search points across collections
        
        for q_str in queries_to_run:
            print(f"[DEBUG RAG] -> Searching vectors for: '{q_str}'")
            # ^ Debug log showing the exact query string being searched
            query_vector = self.embed_model.get_query_embedding(q_str)
            # ^ Translates search string into vector array
            
            for coll in self.collections:
            # ^ Loops through every collection name in the list
                search_res = self.client.query_points(
                # ^ Calls Qdrant client to execute vector query search
                    collection_name=coll, 
                    query=query_vector, 
                    limit=self.similarity_top_k * 2 if self.enable_reranking else self.similarity_top_k
                    # ^ Fetch more results initially if we plan to rerank them down
                )
                
                for p in search_res.points:
                    all_points_dict[p.id] = p
                    # ^ Deduplicates points using point ID as key
        
        all_points = list(all_points_dict.values())
        print(f"[DEBUG RAG] Finished searching. Deduplicated to {len(all_points)} unique points fetched from database.")
        # ^ Debug log showing total unique documents retrieved before reranking
        
        if self.enable_reranking and self.reranker and all_points:
            print(f"[DEBUG RAG] CrossEncoder Reranking is ENABLED. Reranking {len(all_points)} points...")
            # ^ Debug log indicating CrossEncoder process has started
            # Prepare pairs of (query, document) for the CrossEncoder
            pairs = []
            for p in all_points:
                doc_text = p.payload.get("answer", p.payload.get("text", ""))
                pairs.append((query_bundle.query_str, doc_text))
            
            # Predict similarity scores using CrossEncoder
            scores = self.reranker.predict(pairs)
            
            # Update the points with their new cross-encoder scores
            for i, p in enumerate(all_points):
                p.score = float(scores[i])
            
            # Sort by new scores and slice to top-k
            all_points.sort(key=lambda x: x.score, reverse=True)
            
            # TinyBERT outputs raw logits (can be negative even for relevant text).
            # We remove the strict > 0.0 threshold and just rely on top_k sorting.
            all_points = all_points[:self.similarity_top_k]
            
            if all_points:
            # ^ Conditional block preventing index out of bounds error
                print(f"[DEBUG RAG] Post-Reranking complete. Top score: {all_points[0].score:.4f} | Retained top {len(all_points)} most relevant points.")
                # ^ Debug log showing top score and count after filtering
            else:
                print("[DEBUG RAG] Post-Reranking complete. Warning: No points were returned from database.")
                # ^ Debug log warning if db was completely empty
        else:
            print("[DEBUG RAG] CrossEncoder Reranking is DISABLED. Falling back to default vector similarity sorting.")
            # ^ Debug log indicating default execution path
            # Standard vector similarity sorting
            all_points.sort(key=lambda x: x.score, reverse=True)
            all_points = all_points[:self.similarity_top_k]
            if all_points:
            # ^ Conditional block preventing index out of bounds error
                print(f"[DEBUG RAG] Default sorting complete. Top score: {all_points[0].score:.4f} | Retained top {len(all_points)} points.")
                # ^ Debug log showing default top score


        
        nodes = []
        # ^ Initializes empty list to collect compiled NodeWithScore objects
        for p in all_points:
        # ^ Loops through the sorted top points
            question = p.payload.get("question", "")
            # ^ Extracts original question text from point metadata payload
            answer = p.payload.get("answer", "")
            # ^ Extracts fatwa answer text from point metadata payload
            q_and_a = p.payload.get("question_and_answer", "")
            # ^ Extracts combined question and answer used by Deoband collection
            text = p.payload.get("text", "")
            # ^ Extracts legacy text field from point metadata payload as fallback
            
            combined_text = ""
            # ^ Initializes string variable to build node text
            if question or answer:
            # ^ Conditional check, validating if qa payload values are present
                combined_text = f"Question: {question}\nAnswer: {answer}"
                # ^ Merges question and answer strings together
            elif q_and_a:
            # ^ Fallback to check if Deoband combined field exists
                combined_text = q_and_a
                # ^ Uses Deoband combined string
            else:
            # ^ Fallback execution path
                combined_text = text
                # ^ Uses legacy text string if qa fields are missing
                
            node = TextNode(text=combined_text, metadata=p.payload)
            # ^ Compiles new TextNode containing combined texts and original metadata payload
            nodes.append(NodeWithScore(node=node, score=p.score))
            # ^ Appends compiled node wrapped in node-score class container to list
            
        if nodes:
        # ^ Validates if node list is not empty
            print(f"\n[DEBUG RAG] --- ALL FINAL RETRIEVED CONTEXTS ({len(nodes)}) ---")
            # ^ Debug header indicating how many contexts are being passed to LLM
            for i, n in enumerate(nodes):
            # ^ Loops through all final retrieved nodes passed to LLM
                source_url = n.node.metadata.get('url', 'Unknown URL')
                # ^ Extracts URL string for debugging
                
                raw_text = n.node.text.replace('\n', ' ')
                text_snippet = raw_text[:200] + "..." if len(raw_text) > 200 else raw_text
                # ^ Extracts the actual answer text and trims it for terminal readability
                
                print(f"[{i+1}] Score: {n.score:.4f} | Source: {source_url}")
                # ^ Prints rank, score and url for the result
                print(f"    Snippet: {text_snippet}")
                # ^ Prints the actual text snippet that the LLM will read
            print(f"[DEBUG RAG] ----------------------------------------\n")
            # ^ Debug footer
            
        return nodes
        # ^ Returns the list of retrieved nodes
# ^ Ends MultiCollectionRetriever class definition

# 🏛️ CONCEPT: RAG pipelines orchestrate database lookups, query translations, and LLM text generation.
#    They bind data retrieval steps directly into the prompting window of the language model.
# 🏛️ ANALOGY: RagPipeline is like a smart automated legal research system.
#    It takes the user's question, expands it with related legal terms (query expansion),
#    looks up old case laws in the database (retrieval), filters the relevant sections (postprocessor),
#    passes the law books to a judge (LLM) with instructions to write a final judgment citing sources, and outputs the ruling.
class RagPipeline:
# ^ Declares class RagPipeline managing QA pipeline workflows
    def __init__(self):
    # ^ Constructor initializing search components and reasoning LLMs
        self.client = QdrantClient(host=QDRANT_HOST, port=6333)
        # ^ Instantiates Qdrant client connection pointing to port 6333
        
        self.vector_store = QdrantVectorStore(
        # ^ Creates vector database adapter instance
            client=self.client, 
            # ^ Plugs client driver connection
            collection_name=COLLECTION_NAME,
            # ^ Configures collection name target
            text_key="answer"
            # ^ Points search text lookup reader to the 'answer' payload property
        )
        # ^ Ends QdrantVectorStore definition
        
        self.embed_model = HuggingFaceEmbedding(model_name=EMBED_MODEL_NAME, device="cpu")
        # ^ Loads local embedding model on the CPU to save GPU VRAM space
        
        if os.environ.get("NVIDIA_API_KEY"):
        # ^ Conditional check verifying if cloud Nvidia credentials key is present
            Settings.llm = OpenAILike(
            # ^ Instantiates OpenAI-compliant API wrapper targeting Nvidia cloud services
                model=NVIDIA_MODEL_NAME,
                # ^ Configures primary 120B NIM model name target
                api_base=NVIDIA_BASE_URL,
                # ^ Points api base URL to Nvidia integrate gateway
                api_key=os.environ.get("NVIDIA_API_KEY"),
                # ^ Authenticates using Nvidia API key
                is_chat_model=True,
                # ^ Flags that the target API endpoint handles chat completions
                context_window=128000,
                # ^ Sets the model's context window limit to 128k tokens
                temperature=0.0
                # ^ Enforces absolute 0.0 temperature to prevent creative hallucinations
            )
            # ^ Ends OpenAILike configuration
        else:
        # ^ Fallback execution path if Nvidia key is missing
            print("[!] NVIDIA_API_KEY not found in environment. Falling back to local Ollama...")
            # ^ Prints notification alert to system console log
            Settings.llm = Ollama(
            # ^ Instantiates local Ollama LLM wrapper instance
                model=LLM_MODEL_NAME, 
                # ^ Configures local Qwen3 4B model target
                base_url="http://localhost:11434", 
                # ^ Points client requests to local port 11434
                request_timeout=300.0,
                # ^ Sets connection timeout window limit to 300 seconds
                context_window=2048,
                # ^ Configures context window limit to 2048 tokens
                temperature=0.0
                # ^ Enforces absolute 0.0 temperature to prevent creative hallucinations
            )
            # ^ Ends Ollama configuration
        Settings.embed_model = self.embed_model
        # ^ Assigns our embedding model globally to LlamaIndex pipelines
# ^ Ends constructor block

    def _generate_search_query(self, user_question: str, enable_multi_query: bool = False) -> List[str]:
    # ^ Internal method to analyze and expand user questions before database searches
        if enable_multi_query:
            prompt = (
                f"User Question: '{user_question}'\n\n"
                "Task: Generate 3 distinct search queries to look up in an Islamic Fatwa Vector Database.\n"
                "Instructions:\n"
                "1. First query capturing the direct intent.\n"
                "2. Second query using synonyms or Arabic Fiqh terms (e.g. Salah, Wudu).\n"
                "3. Third query focusing on broader prerequisites or scenarios.\n"
                "Return ONLY the 3 queries on separate lines, with no numbering, bullets, or extra text."
            )
            response = str(Settings.llm.complete(prompt)).strip().split('\n')
            queries = [q.strip("- 1234567890.*") for q in response if q.strip()]
            return queries[:3] if queries else [user_question]
        else:
            prompt = (
            # ^ Prompts prompt string block containing instructions for LLM query expansion
                f"User Question: '{user_question}'\n\n"
                # ^ Inserts raw user question text inside prompt string context
                "Task: Rewrite this into a descriptive search query for a Vector Database of Islamic Fatwas.\n"
                "Instructions:\n"
                "1. If the question is just a broad topic (e.g., 'Prayer', 'Salah', 'Fasting', 'Bank Interest'), rewrite it as: "
                "'Specific rulings, conditions, and scenarios regarding [Topic] to identify user intent.'\n"
                "2. If it's a specific question, expand it with synonyms (e.g., 'Wudu' -> 'Ablution, Taharah, washing before prayer').\n"
                "3. Return ONLY the rewritten query text."
            )
            # ^ Ends prompt block configuration
            response = Settings.llm.complete(prompt)
            # ^ Executes completions query call using active LLM configurations
            return [str(response).strip()]
            # ^ Returns stripped string representation of LLM query response
# ^ Ends _generate_search_query function

    def ask(self, user_question: str, chat_history: list = None, sources: list = None, enable_routing=True, enable_multi_query=True, enable_reranking=True):
    # ^ Main interface method routing queries, retrieving context, and running Q&A operations
    
        if enable_routing:
            # The Routing (Receptionist) approach
            route_prompt = (
                f"User Question: '{user_question}'\n\n"
                "Task: Determine if this question is related to Islam, Islamic Fiqh, Quran, Hadith, or general Islamic knowledge.\n"
                "If it is related, output 'YES'. If it is completely unrelated to Islam (e.g., coding, secular politics, math), output 'NO'.\n"
                "Output ONLY 'YES' or 'NO'."
            )
            route_response = str(Settings.llm.complete(route_prompt)).strip().upper()
            if "NO" in route_response:
                return {
                    "answer": "I am a dedicated Islamic Knowledge AI assistant. Your question does not appear to be related to Islam or Islamic Fiqh, so I cannot answer it.",
                    "question": user_question,
                    "expanded_search_query": "N/A (Rejected by Semantic Router)",
                    "sources": [],
                    "is_clarification": False,
                    "error": None
                }
    
        if not sources:
        # ^ Conditional check checking if source filters list is empty
            sources = ["islamqa","deoband"]
            # ^ Defaults filters to search both approved databases if none requested

        search_queries = self._generate_search_query(user_question, enable_multi_query=enable_multi_query)
        # ^ Expands user question into a list of searchable queries using internal generator helper
        print(f"[*] Expanded Search Queries for Qdrant: {search_queries}")
        # ^ Prints expanded query strings to terminal console log

        retriever = MultiCollectionRetriever(
        # ^ Instantiates custom multi-collection retriever helper class
            client=self.client,
            # ^ Supplies the active Qdrant client connection driver instance
            collections=sources,
            # ^ Supplies the target sources filters list
            embed_model=self.embed_model,
            # ^ Supplies the active embedding model instance
            similarity_top_k=10,
            # ^ Configures search result window size to top 20 vectors
            search_queries=search_queries,
            enable_reranking=enable_reranking
        )
        # ^ Ends retriever configuration
        
        system_prompt = (
        # ^ Prompts system instructions defining behavior rules for Fiqh assistance
            "You are a strict Islamic Fiqh (jurisprudence) specialist. "
            # ^ Defines agent identity role context
            "Islamic rulings (Fatwas) are highly dependent on specific circumstances. "
            # ^ Asserts situational dependency context
            "IMPORTANT RULE: If the user's question is a broad topic (e.g., 'Prayer', 'Salah', 'Fasting', 'Bank Interest') "
            # ^ Defines rule triggers for general topic inputs
            "without a specific scenario, problem, or question (e.g., 'How to pray while traveling' or 'Is bank interest halal?'), "
            # ^ Examples of specific scenario inputs
            "you MUST FIRST analyze the fatwas (questions and answers) from the provided CONTEXT and give a general answer of that topic.\n"
            # ^ Instruction enforcing general explanation generation based on context
            "AND NEXT: You MUST politely ask the user to specify which exact aspect of the topic that is relevant to the CONTEXT they are asking about. "
            # ^ Instruction enforcing follow-up clarification requests
            "Give them 10-15 examples of specific questions they could ask.\n"
            # ^ Instruction enforcing sample question outputs
            "Only answer once the question is specific enough to provide a source-based ruling. "
            # ^ Instruction enforcing source citation dependencies
            "When answering: Use ONLY the provided context. Answer clearly in the user's language. And do not give any opinions or suggestions from your trained data.\n"
            # ^ Core RAG safety rules preventing model hallucination or opinion generation
            "If no context matches, say: 'I could not find an answer in the approved sources.'\n"
            # ^ Core fallback message requirements
            "Always include references and citations to hadiths, the Quran and source URL wherever possible at the appropriate place in the answer and at the end of your final answers."
            "Always include reference to source URL at the end of every sentence or bulluet point so that it would be easy for the user to refer by visiting the website.\n"
            "The output response should be in a proper editable markdown format that can be rendered easily."
            # ^ Citation URL formatting requirements
        )
        # ^ Ends system prompt block
        
        history_msgs = []
        # ^ Initializes empty list to collect LlamaIndex ChatMessage format messages
        if chat_history:
        # ^ Checks if previous conversation history exists
            for msg in chat_history:
            # ^ Loops through chat turn logs
                role = MessageRole.USER if msg["role"] == "user" else MessageRole.ASSISTANT
                # ^ Maps string roles to LlamaIndex MessageRole enum entities
                history_msgs.append(ChatMessage(role=role, content=msg["content"]))
                # ^ Compiles ChatMessage objects and appends them to message history list
        # ^ Ends history block logic

        memory = ChatMemoryBuffer.from_defaults(token_limit=8000)
        # ^ Instantiates chat context memory buffer setting token limit to 8000
        
        from llama_index.core.chat_engine import ContextChatEngine
        # ^ Import ContextChatEngine locally to prevent startup dependency conflicts
        
        chat_engine = ContextChatEngine.from_defaults(
        # ^ Instantiates context-aware chat engine wrapper class
            retriever=retriever,
            # ^ Supplies custom multi-collection search retriever helper
            memory=memory,
            # ^ Supplies chat memory buffer tracker
            system_prompt=system_prompt,
            # ^ Supplies the Fiqh rules system instructions
            llm=Settings.llm
            # ^ Supplies the active LLM provider configuration
        )
        # ^ Ends chat engine configuration
        
        response = chat_engine.chat(user_question, chat_history=history_msgs)
        # ^ Queries chat engine with raw question text and context history to generate answer
        
        source_urls = []
        # ^ Initializes list to collect reference source website URLs
        if response.source_nodes:
        # ^ Validates if database vector retrieval matched any source nodes
            source_urls = list(set([node.node.metadata.get("url") for node in response.source_nodes if node.node.metadata.get("url")]))
            # ^ Loops through source nodes metadata, extracts url strings, and de-duplicates them using a set
        # ^ Ends sources extraction

        return {
        # ^ Compiles final result dictionary JSON structure returned to client endpoint
            "answer": str(response),
            # ^ Raw answer response text string generated by LLM
            "question": user_question,
            # ^ Original question text string submitted by user
            "expanded_search_query": " | ".join(search_queries),
            # ^ The expanded query string used for database matching
            "sources": source_urls,
            # ^ List containing reference source URLs
            "is_clarification": False,
            # ^ Flag marking clarification triggers (placeholder for future classifier integrations)
            "error": None
            # ^ Error indicator status field
        }
        # ^ Ends results dictionary
# ^ Ends ask function handler

# --- LOCAL DIAGNOSTIC UNIT TESTING ---
if __name__ == "__main__":
# ^ Conditional block executing statements only when python script is run directly in terminal
    print("[*] Initializing RAG Pipeline with NVIDIA NIM Integration...")
    # ^ Prints diagnostic status notification to console terminal log
    pipeline = RagPipeline()
    # ^ Instantiates test pipeline class instance
    
    test_q = "what invalidates fasting in the month of Ramadaan"
    # ^ Defines test question string
    print(f"[*] Testing query: {test_q}")
    # ^ Prints test question to terminal console
    
    result = pipeline.ask(test_q, sources=['deoband', 'islamqa'])
    # ^ Runs ask function querying local pipeline configurations
    print("\n--- AI ANSWER ---")
    # ^ Prints output section title header
    print(result["answer"])
    # ^ Outputs generated AI answer text string to terminal console
    print("\n--- SOURCES ---")
    # ^ Prints sources section title header
    for s in result["sources"]:
    # ^ Loops through reference URLs list
        print(f" - {s}")
        # ^ Outputs source URLs to console terminal
# ^ Ends diagnostic unit test block
