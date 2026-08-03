package com.ravi.eventmind.ai.recommendation.port;

import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;

import java.util.Optional;

public interface HealingRecommendationStore {

    HealingRecommendation save(HealingRecommendation recommendation);

    Optional<HealingRecommendation> findById(String recommendationId);

    /**
     * Atomically moves a recommendation from one status to another. Only succeeds
     * if the recommendation is still in the expected {@code from} status, which
     * makes the transition safe under concurrent approvals.
     *
     * @return {@code true} when the transition was applied, {@code false} when no
     *         row was in the expected state (concurrent change or missing id)
     */
    boolean transition(String recommendationId, RecommendationStatus from, RecommendationStatus to);
}
