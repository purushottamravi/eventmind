package com.ravi.eventmind.ai.approval;

/**
 * The outcome of an approval, with the who and why attached.
 * No brains, only the answer.
 */
public record ApprovalResult(String recommendationId, boolean approved, String approvedBy, String comment) {
}
