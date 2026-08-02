package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.model.HealingRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The placeholder executor that "heals" by logging what it would do.
 * Good enough until real actions exist.
 */
@Slf4j
@Service
public class LoggingHealingExecutor implements HealingExecutor {

    @Override
    public void execute(HealingRecommendation recommendation) {
        log.info("Executing healing action '{}' for recommendation {}",
                recommendation.suggestedAction(), recommendation.recommendationId());
    }
}
