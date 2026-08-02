package com.ravi.eventmind.ai.rag.chunker;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * The deal every chunker has to keep: take documents in, hand out chunks.
 */
public interface DocumentChunker {
    List<Document> chunk(List<Document> documents);

}