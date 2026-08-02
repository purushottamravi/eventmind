package com.ravi.eventmind.observability.logging;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * The JPA entity behind the {@code APPLICATION_LOG} table - one row per logged
 * event, carrying the level, message, exception details and a timestamp.
 */

@Entity
@Data
@Table(name="APPLICATION_LOG")
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    private String level;

    private String logger;

    @Column(columnDefinition = "CLOB")
    private String message;

    @Column(columnDefinition = "CLOB")
    private String exception;

    @Column(name = "CORRELATION_ID", length = 64)
    private String correlationId;
}
