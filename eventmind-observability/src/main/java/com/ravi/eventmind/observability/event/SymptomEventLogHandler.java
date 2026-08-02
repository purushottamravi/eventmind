package com.ravi.eventmind.observability.event;

import com.ravi.eventmind.observability.logging.ApplicationLogService;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Consumes domain events published by the command side and records them as
 * operational log entries.
 *
 * <p>This is the real observability path: the command module never writes into
 * another service's database. {@link SymptomCreatedEvent} reaches this handler
 * through the Axon event stream, is persisted into {@code APPLICATION_LOG}, and is
 * forwarded to the AI module's {@code POST /logs} ingestion boundary (which feeds
 * the RAG knowledge base).</p>
 *
 * <p>The REST correlation ID is carried as a field on {@link SymptomCreatedEvent}
 * and is persisted and forwarded so the same trace can be followed from the
 * original HTTP request.</p>
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
