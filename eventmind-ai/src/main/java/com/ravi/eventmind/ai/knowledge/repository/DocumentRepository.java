package com.ravi.eventmind.ai.knowledge.repository;

import com.ravi.eventmind.ai.knowledge.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
}