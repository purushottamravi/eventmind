package com.ravi.eventmind.query.handler;

import com.ravi.eventmind.query.entity.Symptom;
import com.ravi.eventmind.query.repository.QuerySymptomRepository;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.events.SymptomCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Watches the symptom events go by and writes them into the read model so the query
 * side has something to answer with.
 */
@Component
@Slf4j
@ProcessingGroup("symptom")
public class SymptomEventsHandler {

    private final QuerySymptomRepository repository;

    public SymptomEventsHandler(QuerySymptomRepository repository) {
        this.repository = repository;
    }


    @EventHandler
    public void on(SymptomCreatedEvent event) {
        String correlationId = event.correlationId();
        boolean hasCorrelationId = correlationId != null && !correlationId.isBlank();
        if (hasCorrelationId) {
            MDC.put(CorrelationId.MDC_KEY, correlationId);
        }
        try {
            log.info("========== SymptomCreatedEvent RECEIVED ==========");
            log.info("id={}, name={}", event.id(), event.name());
            Symptom symptom = new Symptom();
            symptom.setId(event.id());
            symptom.setName(event.name());
            symptom.setOrigin(event.origin());
            symptom.setNumberOfOccurance(event.numberOfOccurance());
            repository.save(symptom);
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
