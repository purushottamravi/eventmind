package com.ravi.eventmind.ai.knowledge.repository;

import com.ravi.eventmind.ai.knowledge.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The standard Spring Data access point for knowledge documents.
 * No custom queries here, just the boilerplate CRUD.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}