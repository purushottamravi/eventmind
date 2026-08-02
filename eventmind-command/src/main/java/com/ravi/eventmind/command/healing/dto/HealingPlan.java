package com.ravi.eventmind.command.healing.dto;

public record HealingPlan(String problem, String rootCause, String recommendedAction, boolean requiresApproval) {
}
