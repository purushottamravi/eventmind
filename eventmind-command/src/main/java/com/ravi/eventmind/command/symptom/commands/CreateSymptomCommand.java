package com.ravi.eventmind.command.symptom.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Asks the system to create one symptom. Pure intention, no logic.
 */
public record CreateSymptomCommand(
        @TargetAggregateIdentifier
        String id,
        String name,
        Long origin,
        Integer numberOfOccurance,
        String correlationId) {
}
