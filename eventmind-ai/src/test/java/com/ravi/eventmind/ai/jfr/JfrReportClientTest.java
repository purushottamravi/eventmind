package com.ravi.eventmind.ai.jfr;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Checks the JFR client returns the report on success and null on server errors.
 */
class JfrReportClientTest {

    @Test
    void fetch_shouldReturnReportBody() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8083/jfr/report"))
                .andRespond(withSuccess("JFR REPORT (generated 2026-01-01T00:00:00Z)\n", MediaType.TEXT_PLAIN));

        JfrReportClient client = new JfrReportClient(builder, "http://localhost:8083");

        String report = client.fetch();

        assertEquals("JFR REPORT (generated 2026-01-01T00:00:00Z)\n", report);
        server.verify();
    }

    @Test
    void fetch_shouldReturnNullOnServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8083/jfr/report"))
                .andRespond(withServerError());

        JfrReportClient client = new JfrReportClient(builder, "http://localhost:8083");

        assertNull(client.fetch());
        server.verify();
    }
}
