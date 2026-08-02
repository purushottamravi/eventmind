package com.ravi.eventmind.ai.knowledge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * A row in the knowledge base that comes to life for a moment when we load it.
 * Plain JPA mapping, no behavior to speak of.
 */
@Entity
@Data
@Table(name = "documents")
public class DocumentEntity {

    @Id
    private Long id;

    private String source;

    private String type;

    @Lob
    private String content;

    private String domain;
}
