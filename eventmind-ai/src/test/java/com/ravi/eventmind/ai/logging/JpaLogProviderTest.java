package com.ravi.eventmind.ai.logging;

import com.ravi.eventmind.ai.logging.entity.ApplicationLog;
import com.ravi.eventmind.ai.logging.repository.ApplicationLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies recent logs come back mapped from entities, and fail softly when the store is down.
 */
@ExtendWith(MockitoExtension.class)
class JpaLogProviderTest {

    @Mock
    private ApplicationLogRepository repository;

    @Test
    void recentLogs_shouldMapEntitiesToEntries() {
        ApplicationLog entity = new ApplicationLog();
        entity.setId(7L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLevel("ERROR");
        entity.setMessage("Database timeout detected");
        entity.setException("connect timed out");
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        List<LogEntry> result = new JpaLogProvider(repository).recentLogs(10);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).id());
        assertEquals("ERROR", result.get(0).level());
        assertEquals("connect timed out", result.get(0).exception());
    }

    @Test
    void recentLogs_shouldReturnEmptyWhenStoreFails() {
        when(repository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("db down"));

        assertTrue(new JpaLogProvider(repository).recentLogs(10).isEmpty());
    }
}
