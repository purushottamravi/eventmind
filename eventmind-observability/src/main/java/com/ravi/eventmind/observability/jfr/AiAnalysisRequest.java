package com.ravi.eventmind.observability.jfr;



public record AiAnalysisRequest(
        String symptomId,
        String symptomName,
        String diagnosticReport
) {}
