package com.ravi.eventmind.ai.approval;

public record ApprovalRequest(String recommendationId, ApprovalDecision decision, String approvedBy, String comment) {
}
