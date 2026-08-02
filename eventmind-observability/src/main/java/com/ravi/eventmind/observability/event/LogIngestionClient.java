package com.ravi.eventmind.observability.event;

/**
 * Port for forwarding operational log entries to another EventMind service.
 *
 * <p>The current producer is the AI module's {@code POST /logs} ingestion
 * boundary, which feeds the RAG knowledge base. Pushes are best-effort: an
 * unavailable consumer must never break the event handler that emits them.</p>
 *
 * @param correlationId correlation ID of the originating REST request (may be null)
 */
public interface LogIngestionClient {

    void push(String level, String message, String correlationId);
}
