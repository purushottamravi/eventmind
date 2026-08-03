package com.ravi.eventmind.observability.event.client;

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
 * The default {@link LogIngestionClient}: ships log entries to the AI module's
 * {@code POST /logs} endpoint. Pushes run on their own async thread pool so a slow
 * AI service never stalls the event processor, and the correlation ID travels along
 * as an {@code X-Correlation-Id} header (and into the MDC) to keep the trace intact.
 *
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
