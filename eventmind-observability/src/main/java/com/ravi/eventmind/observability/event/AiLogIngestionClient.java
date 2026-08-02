package com.ravi.eventmind.observability.event;

import com.ravi.eventmind.shared.correlation.CorrelationId;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Default {@link LogIngestionClient} pushing entries to the AI module's
 * {@code POST /logs} ingestion endpoint.
 *
 * <p>The push runs on the {@code logIngestionExecutor} thread pool so a slow or
 * unavailable AI service never stalls the tracking processor, and failures are
 * isolated (logged and skipped) per the failure-isolation design.</p>
 *
 * <p>The correlation ID is forwarded as the {@code X-Correlation-Id} header so the
 * AI module can trace the entry back to the original REST request, and is placed
 * into the MDC of the async thread for its own log lines.</p>
 */
@Slf4j
@Component
public class AiLogIngestionClient implements LogIngestionClient {

    private final RestClient restClient;

    public AiLogIngestionClient(@Value("${eventmind.ai.base-url:http://localhost:8080}") String aiBaseUrl) {
        this.restClient = RestClient.create(aiBaseUrl);
    }

    @Async("logIngestionExecutor")
    @Override
    public void push(String level, String message, String correlationId) {
        boolean hasCorrelationId = correlationId != null && !correlationId.isBlank();
        if (hasCorrelationId) {
            MDC.put(CorrelationId.MDC_KEY, correlationId);
        }
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri("/logs")
                    .contentType(MediaType.APPLICATION_JSON);
            if (hasCorrelationId) {
                spec = spec.header(CorrelationId.HEADER, correlationId);
            }
            spec.body(Map.of("level", level, "message", message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Could not push log entry to eventmind-ai; skipping: {}", e.getMessage());
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
