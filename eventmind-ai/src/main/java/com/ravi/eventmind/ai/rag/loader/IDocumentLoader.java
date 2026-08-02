package com.ravi.eventmind.ai.rag.loader;

import org.springframework.ai.document.Document;

import java.util.List;

public interface IDocumentLoader {
    List<Document> load();
}
