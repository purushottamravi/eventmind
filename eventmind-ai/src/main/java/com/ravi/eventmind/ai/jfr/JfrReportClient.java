package com.ravi.eventmind.ai.jfr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Pulls the live JFR report from the observability module's {@code GET /jfr/report}
 * endpoint so the LLM analysis sees the real JVM signal without the caller handing
 * it over. Best-effort: if observability is down we just run without the report.
 */
@Slf4j
@Component
public class JfrReportClient {

    private final RestClient restClient;

    public JfrReportClient(RestClient.Builder builder,
                           @Value("${eventmind.observability.base-url:http://localhost:8083}") String observabilityBaseUrl) {
        this.restClient = builder.baseUrl(observabilityBaseUrl).build();
    }

    /**
     * @return the rendered JFR report text, or {@code null} when the report could not be fetched
     */
    public String fetch() {
        try {
            return restClient.get()
                    .uri("/jfr/report")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.warn("Could not fetch JFR report from eventmind-observability; analysis will run without it: {}",
                    e.getMessage());
            return null;
        }
    }
}
