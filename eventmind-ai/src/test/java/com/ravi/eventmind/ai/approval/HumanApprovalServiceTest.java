package com.ravi.eventmind.ai.approval;

import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HumanApprovalServiceTest {

    private final HumanApprovalService service = new HumanApprovalService();

    @Test
    void processApproval_shouldApprove() {
        ApprovalResult result = service.processApproval(recommendation(),
                new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go ahead"));

        assertTrue(result.approved());
        assertEquals("r1", result.recommendationId());
        assertEquals("ravi", result.approvedBy());
        assertEquals("go ahead", result.comment());
    }

    @Test
    void processApproval_shouldReject() {
        ApprovalResult result = service.processApproval(recommendation(),
                new ApprovalRequest("r1", ApprovalDecision.REJECTED, "ravi", "no"));

        assertFalse(result.approved());
    }

    private HealingRecommendation recommendation() {
        return new HealingRecommendation("r1", "s1", "OOM", "HIGH",
                "heap", List.of(), "RESTART_SERVICE", 90, true,
                RecommendationStatus.PENDING_APPROVAL);
    }
}
