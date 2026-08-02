package com.ravi.eventmind.ai.knowledge.service;

import com.ravi.eventmind.ai.knowledge.entity.DocumentEntity;
import com.ravi.eventmind.ai.knowledge.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }
    public List<DocumentEntity> getAllDocuments() {
        return repository.findAll();
    }
}
