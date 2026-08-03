package com.ravi.eventmind.ai.logging;

import com.ravi.eventmind.ai.logging.entity.ApplicationLog;
import com.ravi.eventmind.ai.logging.repository.ApplicationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * Checks log entries are persisted with and without an exception and correlation id.
 */
@ExtendWith(MockitoExtension.class)
class JpaLogStoreTest {

    @Mock
    private ApplicationLogRepository repository;

    @Test
    void save_shouldPersistEntityWithException() {
        JpaLogStore store = new JpaLogStore(repository);

        store.save("ERROR", "Execution failed", "cannot restart", "corr-123");

        ArgumentCaptor<ApplicationLog> captor = ArgumentCaptor.forClass(ApplicationLog.class);
        verify(repository).save(captor.capture());
        assertEquals("ERROR", captor.getValue().getLevel());
        assertEquals("Execution failed", captor.getValue().getMessage());
        assertEquals("cannot restart", captor.getValue().getException());
        assertEquals("corr-123", captor.getValue().getCorrelationId());
        assertNotNull(captor.getValue().getCreatedAt());
    }

    @Test
    void save_shouldPersistEntityWithoutException() {
        JpaLogStore store = new JpaLogStore(repository);

        store.save("INFO", "Completed successfully", null, null);

        ArgumentCaptor<ApplicationLog> captor = ArgumentCaptor.forClass(ApplicationLog.class);
        verify(repository).save(captor.capture());
        assertEquals("INFO", captor.getValue().getLevel());
        assertEquals("Completed successfully", captor.getValue().getMessage());
        assertNull(captor.getValue().getException());
        assertNull(captor.getValue().getCorrelationId());
        assertNotNull(captor.getValue().getCreatedAt());
    }
}
