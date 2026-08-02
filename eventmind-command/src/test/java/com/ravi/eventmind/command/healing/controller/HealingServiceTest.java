package com.ravi.eventmind.command.healing.controller;

import com.ravi.eventmind.command.healing.dto.HealingPlan;
import org.junit.jupiter.api.Test;

/**
 * Proves the healing service handles each recommended action without blowing up.
 */
class HealingServiceTest {

    private final HealingService healingService = new HealingService();

    @Test
    void execute_shouldHandleRestartService() {
        HealingPlan plan = plan("RESTART_SERVICE");
        healingService.execute(plan);
    }

    @Test
    void execute_shouldHandleClearCache() {
        HealingPlan plan = plan("CLEAR_CACHE");
        healingService.execute(plan);
    }

    @Test
    void execute_shouldHandleRetryCommand() {
        HealingPlan plan = plan("RETRY_COMMAND");
        healingService.execute(plan);
    }

    @Test
    void execute_shouldIgnoreUnknownAction() {
        HealingPlan plan = plan("UNKNOWN_ACTION");
        healingService.execute(plan);
    }

    private HealingPlan plan(String action) {
        return new HealingPlan("High memory usage", "Retained heap objects", action, true);
    }
}
