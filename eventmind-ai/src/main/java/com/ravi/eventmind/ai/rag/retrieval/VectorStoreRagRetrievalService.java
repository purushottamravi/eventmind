package com.ravi.eventmind.ai.rag.retrieval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VectorStoreRagRetrievalService implements RagRetrievalService {
    private final VectorStore vectorStore;

    public VectorStoreRagRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public List<Document> search(String query, int topK) {
        try {
            return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK).build());
        } catch (Exception e) {
            log.warn("Vector similarity search failed; returning empty knowledge context", e);
            return List.of();
        }
    }
}
