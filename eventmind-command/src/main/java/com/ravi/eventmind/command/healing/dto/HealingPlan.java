package com.ravi.eventmind.command.healing.dto;

/**
 * Just a box we use to move a healing decision around. No brains, only fields.
 */
public record HealingPlan(String problem, String rootCause, String recommendedAction, boolean requiresApproval) {
}
