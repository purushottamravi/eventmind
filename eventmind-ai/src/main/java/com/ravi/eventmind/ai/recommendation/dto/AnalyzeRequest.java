package com.ravi.eventmind.ai.recommendation.dto;

/**
 * Everything the analyze endpoint needs: symptom, optional logs and JFR, topK.
 * A plain envelope for the request.
 */
public record AnalyzeRequest(String symptomId, String symptom, String logs, String jfrReport, Integer topK) {
}
