package com.ravi.eventmind.ai.rag.embedding;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * The contract for anything that can turn documents into vector-store entries.
 */
public interface EmbeddingService {
    void embed(List<Document> documents);

}
