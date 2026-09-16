package com.ravi.eventmind.observability.jfr;

/**
 * The JSON body sent to the AI module's {@code POST /healing/analyze} endpoint.
 * Only the fields the AI schema actually needs are included.
 */
public record AiAnalyzeRequest(String symptomId, String symptom, String jfrReport) {
}
