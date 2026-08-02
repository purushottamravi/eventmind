package com.ravi.eventmind.ai.recommendation;

public record AnalyzeRequest(String symptomId, String symptom, String logs, String jfrReport, Integer topK) {
}
