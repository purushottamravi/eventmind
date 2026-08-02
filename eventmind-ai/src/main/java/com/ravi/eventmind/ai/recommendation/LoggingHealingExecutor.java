package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.model.HealingRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingHealingExecutor implements HealingExecutor {

    @Override
    public void execute(HealingRecommendation recommendation) {
        log.info("Executing healing action '{}' for recommendation {}",
                recommendation.suggestedAction(), recommendation.recommendationId());
    }
}
