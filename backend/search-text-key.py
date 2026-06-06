from qdrant_client import QdrantClient
client = QdrantClient(host="localhost", port=6333)
sample = client.scroll(collection_name="islamqa", limit=1)
print(sample[0][0].payload.keys()) 
