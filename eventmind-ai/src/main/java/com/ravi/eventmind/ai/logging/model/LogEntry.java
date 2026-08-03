package com.ravi.eventmind.ai.logging.model;

/**
 * An immutable snapshot of one application log entry for the AI module's read
 * ports, kept separate from the persistence entity.
 */
public record LogEntry(Long id, String level, String message, String exception) {
}
