package com.ravi.eventmind.query.handler;

import com.ravi.eventmind.query.entity.Symptom;
import com.ravi.eventmind.query.repository.QuerySymptomRepository;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Proves the event handler turns a SymptomCreatedEvent into a saved row, so we don't
 * lose symptoms on the way into the read model.
 */
@ExtendWith(MockitoExtension.class)
class SymptomEventsHandlerTest {

    @Mock
    private QuerySymptomRepository repository;

    @Test
    void on_shouldPersistSymptomFromEvent() {
        SymptomEventsHandler handler = new SymptomEventsHandler(repository);

        String id = "77a1b2c3-4d5e-4f6a-8b7c-9d0e1f2a3b4c";
        SymptomCreatedEvent event = new SymptomCreatedEvent(id, "Database timeout", 4L, 3, "corr-123");

        handler.on(event);

        ArgumentCaptor<Symptom> captor = ArgumentCaptor.forClass(Symptom.class);
        verify(repository).save(captor.capture());

        Symptom saved = captor.getValue();
        assertEquals(id, saved.getId());
        assertEquals("Database timeout", saved.getName());
        assertEquals(4L, saved.getOrigin());
        assertEquals(3, saved.getNumberOfOccurance());
    }
}
