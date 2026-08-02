package com.ravi.eventmind.ai.approval;

public record ApprovalResult(String recommendationId, boolean approved, String approvedBy, String comment) {
}
