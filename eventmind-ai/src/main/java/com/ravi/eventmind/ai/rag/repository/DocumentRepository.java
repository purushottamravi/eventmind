package com.ravi.eventmind.ai.rag.repository;

import com.ravi.eventmind.ai.rag.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The standard Spring Data access point for knowledge documents.
 * No custom queries here, just the boilerplate CRUD.
 */
@Repository
public interface DocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
}
