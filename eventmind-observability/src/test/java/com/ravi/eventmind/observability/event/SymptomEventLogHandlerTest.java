package com.ravi.eventmind.observability.event;

import com.ravi.eventmind.observability.logging.ApplicationLogService;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SymptomEventLogHandlerTest {

    @Mock
    private ApplicationLogService applicationLogService;

    @Mock
    private LogIngestionClient logIngestionClient;

    @Test
    void on_shouldRecordEventLocallyAndForwardToAi() {
        SymptomEventLogHandler handler = new SymptomEventLogHandler(applicationLogService, logIngestionClient);
        SymptomCreatedEvent event = new SymptomCreatedEvent(
                "77a1b2c3-4d5e-4f6a-8b7c-9d0e1f2a3b4c", "Database timeout", 3L, 2, "corr-123");

        handler.on(event);

        verify(applicationLogService).save(eq("INFO"), contains("Database timeout"), isNull(), eq("corr-123"));
        verify(logIngestionClient).push(eq("INFO"), contains("Database timeout"), eq("corr-123"));
    }

    @Test
    void on_shouldTolerateMissingCorrelationId() {
        SymptomEventLogHandler handler = new SymptomEventLogHandler(applicationLogService, logIngestionClient);
        SymptomCreatedEvent event = new SymptomCreatedEvent(
                "77a1b2c3-4d5e-4f6a-8b7c-9d0e1f2a3b4c", "Database timeout", 3L, 2, null);

        handler.on(event);

        verify(applicationLogService).save(eq("INFO"), contains("Database timeout"), isNull(), isNull());
        verify(logIngestionClient).push(eq("INFO"), contains("Database timeout"), isNull());
    }
}
