package com.ravi.eventmind.ai.model;

import java.util.List;

public record HealingRecommendation(String recommendationId,
                                    String symptomId,
                                    String problem,
                                    String severity,
                                    String rootCause,
                                    List<Evidence> evidence,
                                    String suggestedAction,
                                    Integer confidence,
                                    boolean requiresHumanApproval,
                                    RecommendationStatus status) {

    public HealingRecommendation withRecommendationId(String recommendationId) {
        return new HealingRecommendation(recommendationId, symptomId, problem, severity, rootCause,
                evidence, suggestedAction, confidence, requiresHumanApproval, status);
    }

    public HealingRecommendation withSymptomId(String symptomId) {
        return new HealingRecommendation(recommendationId, symptomId, problem, severity, rootCause,
                evidence, suggestedAction, confidence, requiresHumanApproval, status);
    }

    public HealingRecommendation withStatus(RecommendationStatus status) {
        return new HealingRecommendation(recommendationId, symptomId, problem, severity, rootCause,
                evidence, suggestedAction, confidence, requiresHumanApproval, status);
    }
}
