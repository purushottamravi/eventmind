package com.ravi.eventmind.command.healing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.command.healing.dto.HealingPlan;
import com.ravi.eventmind.shared.exceptions.ProblemDetailAdvice;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the healing controller behaves itself, including rejecting bad plans, so we don't break things by accident.
 */
@ExtendWith(MockitoExtension.class)
class HealingControllerTest {

    @Mock
    private HealingService healingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaValidator validator;
        try (InputStream in = new ClassPathResource("schema/healing-plan-request.schema.json").getInputStream()) {
            validator = JsonSchemaValidator.from(in, objectMapper);
        }
        HealingController controller = new HealingController(healingService, validator, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProblemDetailAdvice())
                .build();
    }

    @Test
    void approve_shouldDelegateToHealingService() throws Exception {
        mockMvc.perform(post("/healing/approve")
                        .contentType("application/json")
                        .content("""
                                {
                                  "problem": "High memory usage",
                                  "rootCause": "Retained heap objects",
                                  "recommendedAction": "RESTART_SERVICE",
                                  "requiresApproval": true
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<HealingPlan> captor = ArgumentCaptor.forClass(HealingPlan.class);
        verify(healingService).execute(captor.capture());

        HealingPlan plan = captor.getValue();
        assertEquals("High memory usage", plan.problem());
        assertEquals("Retained heap objects", plan.rootCause());
        assertEquals("RESTART_SERVICE", plan.recommendedAction());
        assertTrue(plan.requiresApproval());
    }

    @Test
    void approve_shouldRejectUnknownAction() throws Exception {
        mockMvc.perform(post("/healing/approve")
                        .contentType("application/json")
                        .content("""
                                {
                                  "problem": "High memory usage",
                                  "rootCause": "Retained heap objects",
                                  "recommendedAction": "DROP_DATABASE",
                                  "requiresApproval": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.recommendedAction: must be one of [\"RESTART_SERVICE\",\"CLEAR_CACHE\",\"RETRY_COMMAND\"]"));
    }

    @Test
    void approve_shouldRejectMissingProblem() throws Exception {
        mockMvc.perform(post("/healing/approve")
                        .contentType("application/json")
                        .content("""
                                {
                                  "rootCause": "Retained heap objects",
                                  "recommendedAction": "RESTART_SERVICE",
                                  "requiresApproval": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$: problem is required"));
    }
}
