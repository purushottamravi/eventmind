package com.ravi.eventmind.command.symptom.aggregate;

import com.ravi.eventmind.command.symptom.commands.CreateSymptomCommand;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

/**
 * Proves the aggregate creates symptoms, applies events, and rejects nonsense input, so we don't break things by accident.
 */
class SymptomAggregateTest {

    @Test
    void whenCommandHandled_shouldApplySymptomCreatedEvent() {
        String id = UUID.randomUUID().toString();
        CreateSymptomCommand command = new CreateSymptomCommand(
                id, "High memory usage", 7L, 4, "corr-1");

        try (MockedStatic<AggregateLifecycle> lifecycle = mockStatic(AggregateLifecycle.class)) {
            SymptomAggregate.create(command);

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            lifecycle.verify(() -> AggregateLifecycle.apply(captor.capture()));

            SymptomCreatedEvent applied = (SymptomCreatedEvent) captor.getValue();
            assertEquals(command.id(), applied.id());
            assertEquals(command.name(), applied.name());
            assertEquals(command.origin(), applied.origin());
            assertEquals(command.numberOfOccurance(), applied.numberOfOccurance());
            assertEquals(command.correlationId(), applied.correlationId());
        }
    }

    @Test
    void whenEventApplied_shouldUpdateAggregateState() {
        SymptomAggregate aggregate = new SymptomAggregate();
        SymptomCreatedEvent event = new SymptomCreatedEvent(
                UUID.randomUUID().toString(), "Database timeout", 3L, 1, null);

        aggregate.on(event);

        assertEquals(event.id(), ReflectionTestUtils.getField(aggregate, "id"));
        assertEquals("Database timeout", ReflectionTestUtils.getField(aggregate, "name"));
        assertEquals(3L, ReflectionTestUtils.getField(aggregate, "origin"));
        assertEquals(1, ReflectionTestUtils.getField(aggregate, "numberOfOccurance"));
    }

    @Test
    void whenNameBlank_shouldRejectCreation() {
        CreateSymptomCommand command = new CreateSymptomCommand(
                UUID.randomUUID().toString(), "   ", 1L, 1, null);

        assertThrows(EventMindExceptions.class, () -> SymptomAggregate.create(command));
    }

    @Test
    void whenNameTooLong_shouldRejectCreation() {
        CreateSymptomCommand command = new CreateSymptomCommand(
                UUID.randomUUID().toString(),
                "x".repeat(SymptomAggregate.NAME_MAX_LENGTH + 1),
                1L,
                1,
                null);

        assertThrows(EventMindExceptions.class, () -> SymptomAggregate.create(command));
    }

    @Test
    void whenOriginNotPositive_shouldRejectCreation() {
        CreateSymptomCommand command = new CreateSymptomCommand(
                UUID.randomUUID().toString(), "Headache", 0L, 1, null);

        assertThrows(EventMindExceptions.class, () -> SymptomAggregate.create(command));
    }

    @Test
    void whenNumberOfOccurrencesNotPositive_shouldRejectCreation() {
        CreateSymptomCommand command = new CreateSymptomCommand(
                UUID.randomUUID().toString(), "Headache", 1L, 0, null);

        assertThrows(EventMindExceptions.class, () -> SymptomAggregate.create(command));
    }

    @Test
    void whenIdNotAUuid_shouldRejectCreation() {
        CreateSymptomCommand command = new CreateSymptomCommand(
                "not-a-uuid", "Headache", 1L, 1, null);

        assertThrows(EventMindExceptions.class, () -> SymptomAggregate.create(command));
    }
}
