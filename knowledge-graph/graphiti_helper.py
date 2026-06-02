# File: knowledge-graph/graphiti_helper.py
# Purpose: Easy-to-use functions for AI agents to save and retrieve project knowledge
#          Run this script to log session info, search project state, or add decisions.
# Usage:
#   python graphiti_helper.py save-session "Completed HomeScreen.kt in Phase 1"
#   python graphiti_helper.py search "current phase"
#   python graphiti_helper.py add-decision "Use Qwen3:4b because it fits 4GB VRAM"
# Developer: Shaik Hidayatullah

# ─── Standard Python library imports ──────────────────────────────────────────
import asyncio
# ^ asyncio is Python's library for running code asynchronously (multiple things at once)
# ^ Graphiti uses async because database queries can take time — async lets other code run meanwhile

import sys
# ^ sys gives access to command-line arguments (what you type after the script name)

from datetime import datetime, timezone
# ^ datetime lets us get the current date and time
# ^ timezone.utc ensures we always store times in UTC (universal time — avoids confusion)

# ─── Import our Graphiti configuration ────────────────────────────────────────
from graphiti_config import create_graphiti_client
# ^ Import the function we wrote in graphiti_config.py that creates a Graphiti client


async def save_session(description: str):
    """
    Saves a session summary to the Graphiti knowledge graph.
    Call this at the END of every AI coding session.

    Parameters:
        description (str): What was done in this session. Be specific.
                           Example: "Phase 1: Created HomeScreen.kt with mic button.
                                    Next: Create AnswerScreen.kt"
    """

    # Create the Graphiti client (connects to Neo4j + Ollama)
    client = create_graphiti_client()
    # ^ This opens connections to Neo4j (port 7687) and Ollama (port 11434)

    # Get the current timestamp
    now = datetime.now(timezone.utc)
    # ^ timezone.utc ensures the time is in UTC format
    # ^ .now() gets the current date and time
    timestamp = now.strftime("%Y-%m-%d %H:%M UTC")
    # ^ strftime converts the datetime object to a human-readable string
    # ^ "%Y-%m-%d %H:%M UTC" format example: "2026-05-28 14:30 UTC"

    # Add the session as an "episode" in the Graphiti knowledge graph
    # An episode is like a diary entry — it has a name, content, and timestamp
    await client.add_episode(
        name=f"dev_session_{now.strftime('%Y%m%d_%H%M')}",
        # ^ Unique name for this episode — uses timestamp to make it unique
        # ^ Example: "dev_session_20260528_1430"
        episode_body=f"[{timestamp}] DEVELOPMENT SESSION\n{description}",
        # ^ The actual content — timestamp + what was done
        source_description="Rushd-ul-Ilm AI agent development session",
        # ^ Describes where this information came from (for context)
        reference_time=now,
        # ^ When this happened — Graphiti uses this for temporal reasoning
    )

    print(f"✅ Session saved to Graphiti: {timestamp}")
    # ^ Confirm success to the terminal

    await client.close()
    # ^ Always close the connection when done — like closing a file after writing to it


async def search_project(query: str):
    """
    Searches the Graphiti knowledge graph for information about the project.
    Use this at the START of a session to understand current project state.

    Parameters:
        query (str): What you want to know.
                     Example: "current phase", "last completed task", "which files exist"
    """

    # Create the Graphiti client
    client = create_graphiti_client()

    # Search the knowledge graph
    results = await client.search(query)
    # ^ search() takes your query text and finds related facts in the graph
    # ^ It uses the embedding model to understand the MEANING of your query
    # ^ Not just keyword matching — it finds semantically similar content too

    # Display results
    print(f"\n🔍 Search results for: '{query}'")
    print("─" * 50)

    if not results:
        # If nothing was found
        print("No results found. Knowledge graph may be empty — run save_session() first.")
    else:
        for i, fact in enumerate(results, 1):
            # Loop through each result and print it
            # enumerate(results, 1) gives us a counter starting from 1
            print(f"\n[{i}] {fact.fact}")
            # ^ fact.fact is the extracted fact text from the graph
            print(f"    Source: {fact.uuid}")
            # ^ Each fact has a unique ID

    await client.close()


async def add_decision(decision: str, reason: str):
    """
    Records an architectural or technical decision in the knowledge graph.
    This helps future AI agents understand WHY certain choices were made.

    Parameters:
        decision (str): What was decided. Example: "Use Qwen3:4b as the LLM"
        reason (str): Why it was decided. Example: "Fits in 4GB VRAM, supports 256K context"
    """

    client = create_graphiti_client()
    now = datetime.now(timezone.utc)

    await client.add_episode(
        name=f"decision_{now.strftime('%Y%m%d_%H%M')}",
        # ^ Name this episode so it's identifiable as a decision
        episode_body=(
            f"ARCHITECTURAL DECISION — {now.strftime('%Y-%m-%d')}\n"
            f"Decision: {decision}\n"
            f"Reason: {reason}\n"
            f"Project: Rushd-ul-Ilm"
        ),
        source_description="Developer architectural decision",
        reference_time=now,
    )

    print(f"✅ Decision logged: {decision}")
    await client.close()


# ─── Command-line interface ────────────────────────────────────────────────────
# This block only runs when the script is executed directly (not imported)
if __name__ == "__main__":
    # Check that the user provided at least a command
    if len(sys.argv) < 2:
        # sys.argv is a list of command-line arguments
        # sys.argv[0] is the script name, sys.argv[1] is the first argument
        print("Usage:")
        print("  python graphiti_helper.py save-session 'Description of what was done'")
        print("  python graphiti_helper.py search 'what you want to find'")
        print("  python graphiti_helper.py add-decision 'decision' 'reason'")
        sys.exit(1)
        # ^ Exit with error code 1 (non-zero = error in Unix)

    # Get the command from command-line arguments
    command = sys.argv[1]
    # ^ sys.argv[1] is the second item — the command name (e.g., "save-session")

    if command == "save-session":
        # Save a session summary
        if len(sys.argv) < 3:
            print("Error: Please provide a session description")
            sys.exit(1)
        description = sys.argv[2]
        # ^ sys.argv[2] is the description text provided after the command
        asyncio.run(save_session(description))
        # ^ asyncio.run() runs an async function synchronously — required for top-level code

    elif command == "search":
        # Search the knowledge graph
        if len(sys.argv) < 3:
            print("Error: Please provide a search query")
            sys.exit(1)
        query = sys.argv[2]
        asyncio.run(search_project(query))

    elif command == "add-decision":
        # Log an architectural decision
        if len(sys.argv) < 4:
            print("Error: Please provide both decision and reason")
            sys.exit(1)
        decision = sys.argv[2]
        # ^ Third argument is the decision
        reason = sys.argv[3]
        # ^ Fourth argument is the reason
        asyncio.run(add_decision(decision, reason))

    else:
        print(f"Unknown command: {command}")
        print("Valid commands: save-session, search, add-decision")
        sys.exit(1)
