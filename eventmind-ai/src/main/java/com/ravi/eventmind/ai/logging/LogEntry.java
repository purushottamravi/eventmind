package com.ravi.eventmind.ai.logging;

/**
 * Immutable view of an application log entry used across the AI module's
 * read ports, decoupled from the persistence entity.
 */
public record LogEntry(Long id, String level, String message, String exception) {
}
