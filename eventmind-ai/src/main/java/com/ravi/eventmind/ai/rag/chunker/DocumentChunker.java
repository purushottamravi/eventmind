package com.ravi.eventmind.ai.rag.chunker;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentChunker {
    List<Document> chunk(List<Document> documents);

}