package com.ravi.eventmind.ai.knowledge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

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
