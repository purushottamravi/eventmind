package com.ravi.eventmind.query.controller;

import com.ravi.eventmind.query.service.SymptomQueryService;
import com.ravi.eventmind.shared.dto.SymptomRestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the controller behaves itself: returns symptoms and handles empty results.
 */
@ExtendWith(MockitoExtension.class)
class SymptomQueryControllerTest {

    @Mock
    private SymptomQueryService symptomQueryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SymptomQueryController controller = new SymptomQueryController(symptomQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void getAllSymptoms_shouldReturnSymptomsFromService() throws Exception {
        SymptomRestModel headache = new SymptomRestModel("symptom-1", "Headache", List.of("A"), 4);
        SymptomRestModel fatigue = new SymptomRestModel("symptom-2", "Fatigue", List.of("B"), 2);

        when(symptomQueryService.findSymptoms(eq(null), eq(0), eq(20)))
                .thenReturn(List.of(headache, fatigue));

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
        when(symptomQueryService.findSymptoms(eq(null), eq(0), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}