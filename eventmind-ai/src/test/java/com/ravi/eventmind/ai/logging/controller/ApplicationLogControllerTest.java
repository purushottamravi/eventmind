package com.ravi.eventmind.ai.logging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.logging.port.LogStore;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.exceptions.ProblemDetailAdvice;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the log-ingest endpoint: happy path, correlation id, and validation rejections.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationLogControllerTest {

    @Mock
    private LogStore logStore;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaValidator validator;
        try (InputStream in = new ClassPathResource("schema/ingest-log-request.schema.json").getInputStream()) {
            validator = JsonSchemaValidator.from(in, objectMapper);
        }
        mockMvc = MockMvcBuilders.standaloneSetup(new ApplicationLogController(logStore, validator, objectMapper))
                .setControllerAdvice(new ProblemDetailAdvice())
                .build();
    }

    @Test
    void ingest_shouldStoreLogEntry() throws Exception {
        mockMvc.perform(post("/logs")
                        .contentType("application/json")
                        .content("""
                                {
                                  "level": "ERROR",
                                  "message": "Database timeout",
                                  "exception": "connect timed out"
                                }
                                """))
                .andExpect(status().isOk());

        verify(logStore).save("ERROR", "Database timeout", "connect timed out", null);
    }

    @Test
    void ingest_shouldStoreCorrelationIdHeader() throws Exception {
        mockMvc.perform(post("/logs")
                        .header(CorrelationId.HEADER, "corr-123")
                        .contentType("application/json")
                        .content("""
                                {
                                  "level": "ERROR",
                                  "message": "Database timeout"
                                }
                                """))
                .andExpect(status().isOk());

        verify(logStore).save("ERROR", "Database timeout", null, "corr-123");
    }

    @Test
    void ingest_shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/logs")
                        .contentType("application/json")
                        .content("""
                                {
                                  "level": "ERROR",
                                  "message": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.message: must match pattern .*\\S.*"));
    }

    @Test
    void ingest_shouldRejectMissingMessage() throws Exception {
        mockMvc.perform(post("/logs")
                        .contentType("application/json")
                        .content("""
                                {
                                  "level": "ERROR"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$: message is required"));
    }
}
