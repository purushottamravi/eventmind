package com.ravi.eventmind.ai.approval;


import com.ravi.eventmind.ai.model.HealingRecommendation;
import org.springframework.stereotype.Service;


/**
 * Looks at the approver's decision and turns it into a proper result.
 * The "yes, go ahead" or "no thanks" part of the flow.
 */
@Service
public class HumanApprovalService {
    public ApprovalResult processApproval(HealingRecommendation recommendation, ApprovalRequest request) {

        if (request.decision() == ApprovalDecision.APPROVED) {

            return new ApprovalResult(
                    recommendation.recommendationId(),
                    true,
                    request.approvedBy(),
                    request.comment()
            );
        }


        return new ApprovalResult(
                recommendation.recommendationId(),
                false,
                request.approvedBy(),
                request.comment()
        );
    }
}
