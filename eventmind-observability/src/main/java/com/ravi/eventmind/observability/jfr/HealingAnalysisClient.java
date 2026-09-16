package com.ravi.eventmind.observability.jfr;

/**
 * Sends the JFR diagnostic snapshot to the AI module's {@code POST /healing/analyze}
 * endpoint so the AI can produce a structured healing recommendation. Pushes are
 * fire-and-forget so a slow or down AI service never stalls the Kafka event processor.
 */
public interface HealingAnalysisClient {

    void analyze(AiAnalyzeRequest request);
}
