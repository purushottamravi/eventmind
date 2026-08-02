package com.ravi.eventmind.ai.rag.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pushes documents into the vector store, which does the embedding heavy lifting.
 * Simple pass-through, nothing fancy.
 */
@Service
public class DefaultEmbeddingService implements EmbeddingService {
    private final VectorStore vectorStore;

    public DefaultEmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    @Override
    public void embed(List<Document> documents) {
        vectorStore.add(documents);
    }
}
