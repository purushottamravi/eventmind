package com.ravi.eventmind.ai.model;

/**
 * Every stage a recommendation can pass through, from created to executed.
 * Keeps the state machine honest.
 */
public enum RecommendationStatus {
    CREATED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    EXECUTED,
    FAILED

}
