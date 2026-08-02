package com.ravi.eventmind.ai.logging;

/**
 * What the log-ingest endpoint accepts: level, message, and optional exception.
 * A plain carrier for that one request.
 */
public record IngestLogRequest(String level, String message, String exception) {
}
