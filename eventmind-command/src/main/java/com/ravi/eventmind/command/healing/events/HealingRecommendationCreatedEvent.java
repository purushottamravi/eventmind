package com.ravi.eventmind.command.healing.events;

/**
 * Tells everyone else "a healing recommendation just got made". Other modules listen and react.
 */
public record HealingRecommendationCreatedEvent(String recommendationId, String symptomId, String suggestedAction, Integer confidence) {
}
