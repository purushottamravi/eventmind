package com.ravi.eventmind.command.symptom.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CreateSymptomCommand(
        @TargetAggregateIdentifier String id,
        String name,
        Long origin,
        Integer numberOfOccurance,
        String correlationId) {
}
