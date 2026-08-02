package com.ravi.eventmind.observability.event;

import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiLogIngestionClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void push_shouldPostLogEntryToAiIngestionBoundary() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> correlationHeader = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/logs", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            correlationHeader.set(exchange.getRequestHeaders().getFirst(CorrelationId.HEADER));
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        server.start();

        AiLogIngestionClient client = new AiLogIngestionClient("http://localhost:" + server.getAddress().getPort());
        client.push("INFO", "Symptom created", "corr-123");

        assertTrue(body.get().contains("\"level\":\"INFO\""), "payload should carry the log level");
        assertTrue(body.get().contains("\"message\":\"Symptom created\""), "payload should carry the message");
        assertEquals("corr-123", correlationHeader.get(), "payload should carry the correlation ID header");
    }

    @Test
    void push_shouldNotThrowWhenAiServiceIsUnavailable() {
        AiLogIngestionClient client = new AiLogIngestionClient("http://localhost:1");

        assertDoesNotThrow(() -> client.push("INFO", "Symptom created", "corr-123"));
    }
}
