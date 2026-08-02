package com.ravi.eventmind.command.symptom.aggregate;

import com.ravi.eventmind.command.symptom.commands.CreateSymptomCommand;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

@Aggregate
@Slf4j
public class SymptomAggregate {

    public static final int NAME_MAX_LENGTH = 120;

    @AggregateIdentifier
    private String id;
    private String name;
    private Long origin;
    private Integer numberOfOccurance;

    public SymptomAggregate() {
    }

    @CommandHandler
    public SymptomAggregate(CreateSymptomCommand command) {
        validateCreation(command);
        AggregateLifecycle.apply(new SymptomCreatedEvent(
                command.id(),
                command.name(),
                command.origin(),
                command.numberOfOccurance(),
                command.correlationId()));
    }

    @CommandHandler
    public void handle(CreateSymptomCommand command) {
        log.info("Symptom {} already exists; ignoring duplicate create command (at-least-once delivery)",
                command.id());
    }

    @EventSourcingHandler
    public void on(SymptomCreatedEvent event) {
        this.id = event.id();
        this.name = event.name();
        this.origin = event.origin();
        this.numberOfOccurance = event.numberOfOccurance();
    }

    private void validateCreation(CreateSymptomCommand command) {
        if (command.id() == null || command.id().isBlank()) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom id must not be blank");
        }
        try {
            UUID.fromString(command.id());
        } catch (IllegalArgumentException e) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom id must be a valid UUID");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom name must not be blank");
        }
        if (command.name().length() > NAME_MAX_LENGTH) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom name must not exceed " + NAME_MAX_LENGTH + " characters");
        }
        if (command.origin() == null || command.origin() <= 0) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom origin must be a positive value");
        }
        if (command.numberOfOccurance() == null || command.numberOfOccurance() <= 0) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "Symptom number of occurrences must be a positive value");
        }
    }
}
