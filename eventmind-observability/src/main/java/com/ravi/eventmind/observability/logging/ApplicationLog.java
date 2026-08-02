package com.ravi.eventmind.observability.logging;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing application execution logs.
 *
 * <p>Stores information about application events including:
 * <ul>
 *     <li>Execution level</li>
 *     <li>Message</li>
 *     <li>Exception details</li>
 *     <li>Creation timestamp</li>
 * </ul>
 *
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
