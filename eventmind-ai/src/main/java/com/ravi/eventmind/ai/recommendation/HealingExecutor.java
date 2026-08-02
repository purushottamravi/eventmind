package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.model.HealingRecommendation;

/**
 * The deal for anything that can carry out a recommendation once it's approved.
 */
public interface HealingExecutor {

    void execute(HealingRecommendation recommendation);
}
