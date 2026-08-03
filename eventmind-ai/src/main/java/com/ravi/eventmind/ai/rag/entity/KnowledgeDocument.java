package com.ravi.eventmind.ai.rag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A row in the knowledge base that comes to life for a moment when we load it.
 * Plain JPA mapping, no behavior to speak of.
 */
@Entity
@Data
@Table(name = "documents")
public class KnowledgeDocument {

    @Id
    private Long id;

    private String source;

    private String type;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String content;

    private String domain;
}
