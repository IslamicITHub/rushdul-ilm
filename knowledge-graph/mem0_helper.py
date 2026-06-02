#!/usr/bin/env python3  # ^ Run this file with Python 3 when it is executed as a script on Linux
# File: knowledge-graph/mem0_helper.py  # ^ This is the Mem0 helper used by the Rushd-ul-Ilm project
# Purpose: Save and retrieve local-only project memories without any cloud LLM calls  # ^ This explains why the script exists
# Usage: python mem0_helper.py save "Completed task..."  # ^ This shows the basic save command for session context
# Usage: python mem0_helper.py search "current phase"  # ^ This shows how to search stored memories later
# Usage: python mem0_helper.py list  # ^ This shows how to print every saved memory for this project
# Usage: python mem0_helper.py save-preference "Always explain MVVM with a simple analogy first"  # ^ This shows how to store stable developer preferences

import os  # ^ We use 'os' to set environment variables and build safe filesystem paths
os.environ["MEM0_TELEMETRY"] = "False"  # ^ Disable Mem0 PostHog telemetry to obey the project's no-third-party-tracking rule

import sys  # ^ We use 'sys' to read command-line arguments like 'save' or 'search'
from pathlib import Path  # ^ Path makes filesystem paths safer and easier to build than raw strings

from mem0 import Memory  # ^ Memory is the main Mem0 class that creates and queries the local memory store

DEVELOPER_ID = "hidayat_rushd_ul_ilm"  # ^ This unique ID keeps this project's memories separate from any other project
AGENT_ID = "rushd_ul_ilm_dev_agent"  # ^ This labels which agent recorded the memory, useful for filtering later
OLLAMA_BASE_URL = "http://localhost:11434"  # ^ Ollama runs locally on this port, so all LLM work stays on the developer's machine
OLLAMA_LLM_MODEL = "phi4-mini:3.8b"  # ^ This is the local small model already installed and fits the developer's GPU limit
OLLAMA_EMBED_MODEL = "nomic-embed-text"  # ^ This local embedding model converts text into vectors for memory search
SCRIPT_DIR = Path(__file__).resolve().parent  # ^ This finds the folder where this script lives, no matter where it is launched from
VECTOR_DB_DIR = SCRIPT_DIR / "mem0_local_db"  # ^ This stores Mem0's local Qdrant data inside the knowledge-graph folder
HISTORY_DB_PATH = SCRIPT_DIR / "mem0_history.db"  # ^ This stores Mem0's local history SQLite file next to the vector store


def get_mem0_client():  # ^ This helper creates one configured Mem0 client for local project memory work
    """Create a local-only Mem0 client for Rushd-ul-Ilm."""  # ^ Short docstring explaining the function's job

    config = {  # ^ Mem0 expects a configuration dictionary that describes the LLM, embedder, and vector store
        "llm": {  # ^ The 'llm' block defines which local model Mem0 uses to extract structured memories
            "provider": "ollama",  # ^ Use Ollama instead of any cloud provider so the project remains fully local
            "config": {  # ^ The nested 'config' block contains the exact runtime settings for the Ollama LLM
                "model": OLLAMA_LLM_MODEL,  # ^ Use the local Qwen 3.5 4B model already installed on this machine
                "ollama_base_url": OLLAMA_BASE_URL,  # ^ Tell Mem0 how to reach the local Ollama server
            },  # ^ End of the local Ollama LLM settings block
        },  # ^ End of the Mem0 LLM provider section
        "embedder": {  # ^ The 'embedder' block defines how text gets converted into vectors for semantic search
            "provider": "ollama",  # ^ Use Ollama embeddings locally so no text leaves the developer's machine
            "config": {  # ^ This nested block holds the exact settings for the embedding model
                "model": OLLAMA_EMBED_MODEL,  # ^ Use the installed nomic embedding model for vector generation
                "ollama_base_url": OLLAMA_BASE_URL,  # ^ Point the embedder at the same local Ollama service
                "embedding_dims": 768,  # ^ This matches the vector size expected by the existing Mem0 local database
            },  # ^ End of the local Ollama embedding settings block
        },  # ^ End of the Mem0 embedder section
        "vector_store": {  # ^ The 'vector_store' block defines where the vectors are stored on disk
            "provider": "qdrant",  # ^ Use Qdrant's local on-disk vector store because it is already part of this project
            "config": {  # ^ This nested block holds the local Qdrant storage settings
                "path": str(VECTOR_DB_DIR),  # ^ Store vector data in an absolute path so launches from other folders still work
                "collection_name": "rushd_ul_ilm_memories",  # ^ Keep all project memories in a named collection for this app
                "embedding_model_dims": 768,  # ^ This must match the embedding vector size used by the Ollama embedder
            },  # ^ End of the local vector store settings block
        },  # ^ End of the Mem0 vector store section
        "history_db_path": str(HISTORY_DB_PATH),  # ^ Save Mem0's history database in a predictable local file
        "version": "v1.1",  # ^ Keep the explicit Mem0 API version stable for repeatable behavior
    }  # ^ End of the full Mem0 configuration dictionary

    client = Memory.from_config(config)  # ^ Build the real Mem0 client object from the local-only configuration above
    return client  # ^ Return the ready-to-use client to the caller


def save_memory(content: str, memory_type: str = "session"):  # ^ This function stores one new memory entry
    """Save a session or preference memory."""  # ^ Short docstring describing the function's job

    client = get_mem0_client()  # ^ Create a configured Mem0 client before trying to save anything
    messages = [  # ^ Mem0's 'add' API expects a list of message objects similar to chat messages
        {  # ^ Start one message dictionary inside the list
            "role": "assistant",  # ^ Mark the source as the assistant that is recording the project memory
            "content": f"[{memory_type.upper()}] {content}",  # ^ Prefix the saved text so later searches can spot session vs preference memories
        }  # ^ End of the single message dictionary
    ]  # ^ End of the message list passed into Mem0

    client.add(  # ^ Ask Mem0 to store durable memory from the message list
        messages=messages,  # ^ Pass the formatted message payload into the Mem0 add operation
        user_id=DEVELOPER_ID,  # ^ Tie this memory to Shaik Hidayatullah's project-specific memory scope
        agent_id=AGENT_ID,  # ^ Record which agent instance wrote the memory
        infer=False,  # ^ Skip LLM-based extraction and store the text directly for faster, deterministic session checkpoints
        metadata={  # ^ Add structured metadata to make filtering and debugging easier later
            "project": "rushd_ul_ilm",  # ^ Tag the memory with the project name
            "memory_type": memory_type,  # ^ Tag whether this was a session memory or a stable preference
        },  # ^ End of the metadata dictionary
    )  # ^ End of the Mem0 add call

    print(f"✅ Memory saved (type: {memory_type})")  # ^ Give terminal feedback that the save operation succeeded
    print(f"   Content: {content[:160]}...")  # ^ Print a short preview so the developer can verify the saved entry


def search_memory(query: str):  # ^ This function searches saved memories using semantic similarity
    """Search stored memories for relevant context."""  # ^ Short docstring describing the function's job

    client = get_mem0_client()  # ^ Create the configured Mem0 client before running a search
    results = client.search(  # ^ Ask Mem0 to find the most relevant memories for the query text
        query=query,  # ^ This is the phrase the developer wants to search for
        filters={"user_id": DEVELOPER_ID},  # ^ Limit the search to this project's own memory scope
        limit=5,  # ^ Keep the output short and useful by returning at most five matches
    )  # ^ End of the Mem0 search call

    print(f"\n🔍 Memories matching: '{query}'")  # ^ Print a clear heading so terminal output is easier to read
    print("─" * 60)  # ^ Print a separator line for visual structure in the terminal

    if not results or not results.get("results"):  # ^ Handle the case where Mem0 does not return any stored matches
        print("No memories found. Save a memory first with: python mem0_helper.py save '...'")  # ^ Tell the developer exactly what to do next
        return  # ^ Stop here because there is nothing useful to print

    for index, memory in enumerate(results["results"], 1):  # ^ Loop through each returned memory and number them from 1
        print(f"\n[{index}] {memory['memory']}")  # ^ Print the actual extracted memory text for the developer to read
        score = memory.get("score", 0)  # ^ Read the similarity score and default to 0 if Mem0 omits it
        print(f"    Relevance: {score:.2f}")  # ^ Show how strongly Mem0 thinks this memory matches the query


def list_all_memories():  # ^ This function prints every stored memory for the current project scope
    """List every saved memory for this project."""  # ^ Short docstring describing the function's job

    client = get_mem0_client()  # ^ Create the configured Mem0 client before fetching all memories
    results = client.get_all(  # ^ Ask Mem0 for every stored memory belonging to this developer scope
        filters={"user_id": DEVELOPER_ID},  # ^ Restrict the fetch to Rushd-ul-Ilm's developer memory space
    )  # ^ End of the Mem0 get_all call

    print(f"\n📋 All saved memories for Rushd-ul-Ilm project:")  # ^ Print a heading for the full memory list output
    print("─" * 60)  # ^ Print a separator line to keep terminal output readable

    memories = results.get("results", [])  # ^ Extract the list of memory rows from the Mem0 response dictionary

    if not memories:  # ^ Handle the empty-state case where nothing has been saved yet
        print("No memories saved yet.")  # ^ Tell the developer the store is still empty
        return  # ^ Stop here because there is nothing else to print

    print(f"Total memories: {len(memories)}")  # ^ Print the total count so the developer sees how much state is stored

    for index, memory in enumerate(memories, 1):  # ^ Loop through each saved memory and number them for easier reference
        print(f"\n[{index}] {memory['memory']}")  # ^ Print the stored memory text itself


def save_preference(preference: str):  # ^ This wrapper stores a stable developer preference instead of a one-off session note
    """Save a durable developer preference."""  # ^ Short docstring describing this specialized save helper

    save_memory(preference, memory_type="preference")  # ^ Reuse the general save function but label the memory as a preference


if __name__ == "__main__":  # ^ This block runs only when the file is executed directly from the terminal
    if len(sys.argv) < 2:  # ^ If no command was supplied, show usage instructions instead of crashing
        print("Usage:")  # ^ Start the help output with a short heading
        print("  python mem0_helper.py save 'what was done this session'")  # ^ Show how to save a session summary
        print("  python mem0_helper.py search 'what you want to find'")  # ^ Show how to search saved memories
        print("  python mem0_helper.py list")  # ^ Show how to list every saved memory
        print("  python mem0_helper.py save-preference 'developer preference'")  # ^ Show how to save a durable preference
        sys.exit(1)  # ^ Exit with a non-zero code because the command was incomplete

    command = sys.argv[1]  # ^ Read the first real command-line argument after the script name

    if command == "save":  # ^ Route the 'save' command to the session-memory save function
        if len(sys.argv) < 3:  # ^ Saving requires actual text content after the command
            print("Error: Provide content to save")  # ^ Tell the developer what is missing
            sys.exit(1)  # ^ Exit with an error status because the command was incomplete
        save_memory(sys.argv[2])  # ^ Save the provided text as a session memory
    elif command == "search":  # ^ Route the 'search' command to the semantic memory search function
        if len(sys.argv) < 3:  # ^ Searching requires a query string after the command
            print("Error: Provide a search query")  # ^ Tell the developer what is missing
            sys.exit(1)  # ^ Exit with an error status because the command was incomplete
        search_memory(sys.argv[2])  # ^ Run a semantic search over the stored project memories
    elif command == "list":  # ^ Route the 'list' command to the function that prints all memories
        list_all_memories()  # ^ Print every stored memory for this project scope
    elif command == "save-preference":  # ^ Route the 'save-preference' command to the preference save helper
        if len(sys.argv) < 3:  # ^ Preference saving also needs actual text after the command
            print("Error: Provide a preference to save")  # ^ Tell the developer what is missing
            sys.exit(1)  # ^ Exit with an error status because the command was incomplete
        save_preference(sys.argv[2])  # ^ Save the provided text as a durable developer preference
    else:  # ^ Catch any unsupported command so the script fails clearly instead of silently
        print(f"Unknown command: {command}")  # ^ Print the unsupported command so the mistake is obvious
        sys.exit(1)  # ^ Exit with an error status because the command name was not recognized
