package com.ravi.eventmind.ai.logging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * One application log row owned by the AI module. We keep our own copy of the
 * operational feed instead of reading the observability module's table directly,
 * so each service stays the owner of its data and schema.
 */
@Entity
@Data
@Table(name = "AI_APPLICATION_LOG")
public class ApplicationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private String level;

    @Column(columnDefinition = "CLOB")
    private String message;

    @Column(columnDefinition = "CLOB")
    private String exception;

    @Column(name = "CORRELATION_ID", length = 64)
    private String correlationId;
}
