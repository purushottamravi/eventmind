package com.ravi.eventmind.ai.logging;

/**
 * How the AI module writes to its application log store.
 */
public interface LogStore {

    void save(String level, String message, String exception, String correlationId);
}
