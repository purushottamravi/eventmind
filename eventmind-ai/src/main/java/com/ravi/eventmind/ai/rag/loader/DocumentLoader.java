package com.ravi.eventmind.ai.rag.loader;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * The promise every document source keeps: give me all documents you can find.
 */
public interface DocumentLoader {
    List<Document> load();
}
