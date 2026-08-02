package com.ravi.eventmind.observability.event.client;

/**
 * Port for forwarding operational log entries to another EventMind service
 * (today that's the AI module's {@code POST /logs} endpoint). Pushes are
 * best-effort - a down consumer must never take down the handler that emits them.
 */
public interface LogIngestionClient {

    void push(String level, String message, String correlationId);
}
