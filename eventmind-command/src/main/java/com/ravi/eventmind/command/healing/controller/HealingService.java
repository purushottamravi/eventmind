package com.ravi.eventmind.command.healing.controller;


import com.ravi.eventmind.command.healing.dto.HealingPlan;
import org.springframework.stereotype.Service;

@Service
public class HealingService {

    public void execute(HealingPlan plan) {

        switch (plan.recommendedAction()) {

            case "RESTART_SERVICE":
                restartService();
                break;

            case "CLEAR_CACHE":
                clearCache();
                break;

            case "RETRY_COMMAND":
                retryFailedCommand();
                break;
        }
    }

    private void restartService() {
    }

    private void retryFailedCommand() {
    }

    private void clearCache(){}
}