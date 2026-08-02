package com.ravi.eventmind.command.healing.events;

public record HealingRecommendationCreatedEvent(String recommendationId, String symptomId, String suggestedAction, Integer confidence) {
}
