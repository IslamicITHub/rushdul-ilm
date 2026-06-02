import asyncio
from graphiti_config import create_graphiti_client

async def check_graphiti():
    try:
        print("Initializing Graphiti client...")
        client = create_graphiti_client()
        
        print("\nFetching recent episodes...")
        # Since get_episodes might not be directly exposed or paginated, we'll try a search or check nodes
        # Let's search for recent MT updates to see what actually made it in.
        
        queries = [
            "micro-task P1.S2.SS1.MT1",
            "micro-task P1.S2.SS1.MT2",
            "micro-task P1.S2.SS2.MT1"
        ]
        
        found_data = False
        for query in queries:
            print(f"\nSearching for: '{query}'")
            results = await client.search(query)
            if results and hasattr(results, 'edges') and results.edges:
                print(f"✅ Found edges related to: {query}")
                for edge in results.edges[:2]: # Show first 2
                    print(f"   - {edge}")
                found_data = True
            elif results and hasattr(results, 'nodes') and results.nodes:
                print(f"✅ Found nodes related to: {query}")
                found_data = True
            else:
                print(f"❌ No data found for: {query}")
                
        if not found_data:
            print("\n⚠️ It appears the recent updates (MT1, MT2, SS2.MT1) were NOT saved to Graphiti due to the rate limits.")
            
    except Exception as e:
        print(f"Error checking Graphiti: {e}")
    finally:
        if 'client' in locals():
            client.close()

if __name__ == "__main__":
    asyncio.run(check_graphiti())
