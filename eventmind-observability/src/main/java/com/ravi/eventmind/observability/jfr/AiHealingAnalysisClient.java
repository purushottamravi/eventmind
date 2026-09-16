package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.shared.correlation.CorrelationId;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * The default {@link HealingAnalysisClient}: pushes the JFR diagnostic snapshot
 * to the AI module's {@code POST /healing/analyze} endpoint. Runs on its own
 * async thread pool so a slow or down AI service never stalls the event processor.
 */
@Slf4j
@Component
public class AiHealingAnalysisClient implements HealingAnalysisClient {

    private final RestClient restClient;

    public AiHealingAnalysisClient(@Value("${eventmind.ai.base-url:http://localhost:8080}") String aiBaseUrl) {
        this.restClient = RestClient.create(aiBaseUrl);
    }

    @Async("aiAnalysisExecutor")
    @Override
    public void analyze(AiAnalyzeRequest request) {
        String correlationId = CorrelationId.get();
        boolean hasCorrelationId = correlationId != null && !correlationId.isBlank();
        if (hasCorrelationId) {
            MDC.put(CorrelationId.MDC_KEY, correlationId);
        }
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri("/healing/analyze")
                    .contentType(MediaType.APPLICATION_JSON);
            if (hasCorrelationId) {
                spec = spec.header(CorrelationId.HEADER, correlationId);
            }
            spec.body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Could not push JFR diagnostic snapshot to eventmind-ai healing endpoint; skipping: {}", e.getMessage());
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
