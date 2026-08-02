package com.ravi.eventmind.ai.logging;

/**
 * Write port for the AI module's application log store.
 */
public interface LogStore {

    void save(String level, String message, String exception, String correlationId);
}
