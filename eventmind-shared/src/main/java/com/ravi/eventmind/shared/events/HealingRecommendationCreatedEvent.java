package com.ravi.eventmind.shared.events;

public record HealingRecommendationCreatedEvent(
        String symptomId,
        String healingAction,
        double confidenceScore,
        String status
) {}
