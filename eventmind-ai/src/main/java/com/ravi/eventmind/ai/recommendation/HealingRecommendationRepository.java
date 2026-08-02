package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.model.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data handle on recommendations, plus a guarded update to flip status atomically.
 */
@Repository
public interface HealingRecommendationRepository extends JpaRepository<HealingRecommendationEntity, String> {

    @Modifying
    @Query("update HealingRecommendationEntity h set h.status = :to, h.version = h.version + 1 "
            + "where h.recommendationId = :id and h.status = :from")
    int transitionStatus(@Param("id") String id,
                         @Param("from") RecommendationStatus from,
                         @Param("to") RecommendationStatus to);
}
