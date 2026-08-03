package com.ravi.eventmind.ai.rag.service;

import com.ravi.eventmind.ai.rag.entity.KnowledgeDocument;
import com.ravi.eventmind.ai.rag.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Hands back the stored knowledge documents when someone asks.
 * Thin wrapper around the repository, nothing more.
 */
@Service
public class DocumentService {
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }
    public List<KnowledgeDocument> getAllDocuments() {
        return repository.findAll();
    }
}
