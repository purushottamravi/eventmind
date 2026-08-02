package com.ravi.eventmind.observability.event;

import com.ravi.eventmind.observability.event.client.LogIngestionClient;
import com.ravi.eventmind.observability.logging.ApplicationLogService;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Turns incoming {@link SymptomCreatedEvent} events into persisted
 * {@code APPLICATION_LOG} rows and forwards them to the AI module. The correlation
 * ID rides along on the event so the whole trace stays followable back to the
 * original HTTP request.
 */
@Slf4j
@Component
@ProcessingGroup("observability")
public class SymptomEventLogHandler {

    private final ApplicationLogService applicationLogService;
    private final LogIngestionClient logIngestionClient;

    public SymptomEventLogHandler(ApplicationLogService applicationLogService,
                                  LogIngestionClient logIngestionClient) {
        this.applicationLogService = applicationLogService;
        this.logIngestionClient = logIngestionClient;
    }

    @EventHandler
    public void on(SymptomCreatedEvent event) {
        String correlationId = event.correlationId();
        boolean hasCorrelationId = correlationId != null && !correlationId.isBlank();
        if (hasCorrelationId) {
            MDC.put(CorrelationId.MDC_KEY, correlationId);
        }
        try {
            String message = String.format("Symptom created: id=%s, name=%s, origin=%s, occurrences=%s",
                    event.id(), event.name(), event.origin(), event.numberOfOccurance());
            log.info("Recording symptom creation into application log");
            applicationLogService.save("INFO", message, null, correlationId);
            logIngestionClient.push("INFO", message, correlationId);
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
