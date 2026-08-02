package com.ravi.eventmind.ai.rag.retrieval;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * The contract for search: query in, matching documents out.
 */
public interface RagRetrievalService {

    List<Document> search(String query, int topK);
}
