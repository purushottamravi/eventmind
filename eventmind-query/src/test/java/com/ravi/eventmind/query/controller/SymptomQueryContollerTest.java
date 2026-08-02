package com.ravi.eventmind.query.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import com.ravi.eventmind.shared.exceptions.ProblemDetailAdvice;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SymptomQueryContollerTest {

    @Mock
    private QueryGateway queryGateway;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaValidator validator;
        try (InputStream in = new ClassPathResource("schema/symptom-query-request.schema.json").getInputStream()) {
            validator = JsonSchemaValidator.from(in, objectMapper);
        }
        SymptomQueryContoller controller = new SymptomQueryContoller(queryGateway, validator, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProblemDetailAdvice())
                .build();
    }

    @Test
    void getAllSymptoms_shouldReturnSymptomsFromQueryGateway() throws Exception {
        SymptomRestModel headache = new SymptomRestModel("symptom-1", "Headache", List.of("A"), 4);
        SymptomRestModel fatigue = new SymptomRestModel("symptom-2", "Fatigue", List.of("B"), 2);

        when(queryGateway.query(any(), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(List.of(headache, fatigue)));

        mockMvc.perform(get("/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("symptom-1"))
                .andExpect(jsonPath("$[0].name").value("Headache"))
                .andExpect(jsonPath("$[0].origin[0]").value("A"))
                .andExpect(jsonPath("$[0].numberOfOccurance").value(4))
                .andExpect(jsonPath("$[1].name").value("Fatigue"));
    }

    @Test
    void getAllSymptoms_shouldReturnEmptyList() throws Exception {
        when(queryGateway.query(any(), any(ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        mockMvc.perform(get("/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllSymptoms_shouldRejectOversizedPageSize() throws Exception {
        mockMvc.perform(get("/symptoms").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.size: must be less than or equal to 100"));
    }

    @Test
    void getAllSymptoms_shouldRejectNegativePage() throws Exception {
        mockMvc.perform(get("/symptoms").param("page", "-3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.page: must be greater than or equal to 0"));
    }

    @Test
    void getAllSymptoms_shouldRejectNonNumericPage() throws Exception {
        mockMvc.perform(get("/symptoms").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"));
    }
}
