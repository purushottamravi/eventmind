package com.ravi.eventmind.ai.approval;

/**
 * What the approver sends in: which recommendation, their call, who they are.
 * Just a box to move the approval decision in.
 */
public record ApprovalRequest(String recommendationId, ApprovalDecision decision, String approvedBy, String comment) {
}
