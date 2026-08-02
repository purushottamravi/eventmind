package com.ravi.eventmind.ai.rag.embedding;

import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbeddingService {
    void embed(List<Document> documents);

}
