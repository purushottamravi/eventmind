package com.ravi.eventmind.shared.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
public record CreateHealingRecommendationCommand(
        @TargetAggregateIdentifier
        String symptomId, // Links it to the original symptom
        String healingAction,                      // The code/text fix from the LLM
        double confidenceScore,                    // The matching percentage (0.0 to 1.0)
        String status                              // Initialized to "PENDING_APPROVAL"
) {}