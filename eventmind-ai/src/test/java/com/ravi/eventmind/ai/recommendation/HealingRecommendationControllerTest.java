package com.ravi.eventmind.ai.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.approval.ApprovalResult;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import com.ravi.eventmind.ai.rag.ingestion.RagIngestionService;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealingRecommendationControllerTest {

    @Mock
    private HealingRecommendationService healingRecommendationService;

    @Mock
    private RagIngestionService ragIngestionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaValidator analyzeValidator;
        JsonSchemaValidator approvalValidator;
        try (InputStream in = new ClassPathResource("schema/analyze-request.schema.json").getInputStream()) {
            analyzeValidator = JsonSchemaValidator.from(in, objectMapper);
        }
        try (InputStream in = new ClassPathResource("schema/approval-request.schema.json").getInputStream()) {
            approvalValidator = JsonSchemaValidator.from(in, objectMapper);
        }
        HealingRecommendationController controller = new HealingRecommendationController(
                healingRecommendationService, ragIngestionService, analyzeValidator, approvalValidator, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ProblemDetailAdvice())
                .build();
    }

    @Test
    void analyze_shouldReturnRecommendation() throws Exception {
        HealingRecommendation recommendation = new HealingRecommendation("r1", "s1", "OOM", "HIGH",
                "heap", List.of(), "RESTART_SERVICE", 90, true, RecommendationStatus.PENDING_APPROVAL);
        when(healingRecommendationService.analyze(any(AnalyzeRequest.class))).thenReturn(recommendation);

        mockMvc.perform(post("/healing/analyze")
                        .contentType("application/json")
                        .content("""
                                {
                                  "symptom": "OOM",
                                  "jfrReport": "GC paused"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationId").value("r1"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void analyze_shouldRejectMissingSymptom() throws Exception {
        mockMvc.perform(post("/healing/analyze")
                        .contentType("application/json")
                        .content("""
                                {
                                  "jfrReport": "GC paused"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$: symptom is required"));
    }

    @Test
    void getRecommendation_shouldReturnRecommendation() throws Exception {
        HealingRecommendation recommendation = new HealingRecommendation("r1", "s1", "OOM", "HIGH",
                "heap", List.of(), "RESTART_SERVICE", 90, true, RecommendationStatus.PENDING_APPROVAL);
        when(healingRecommendationService.findById("r1")).thenReturn(recommendation);

        mockMvc.perform(get("/healing/recommendations/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationId").value("r1"));
    }

    @Test
    void getRecommendation_shouldReturnNotFoundWhenMissing() throws Exception {
        when(healingRecommendationService.findById("missing"))
                .thenThrow(new EventMindExceptions(
                        EventMindReasonsEnum.RECOMMENDATION_NOT_FOUND, "Recommendation not found: missing"));

        mockMvc.perform(get("/healing/recommendations/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("C005"))
                .andExpect(jsonPath("$.key").value("recommendation.not.found"));
    }

    @Test
    void approve_shouldReturnApprovalResult() throws Exception {
        when(healingRecommendationService.approve(any(com.ravi.eventmind.ai.approval.ApprovalRequest.class)))
                .thenReturn(new ApprovalResult("r1", true, "ravi", "go ahead"));

        mockMvc.perform(post("/healing/approve")
                        .contentType("application/json")
                        .content("""
                                {
                                  "recommendationId": "r1",
                                  "decision": "APPROVED",
                                  "approvedBy": "ravi",
                                  "comment": "go ahead"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true));
    }

    @Test
    void approve_shouldRejectUnknownDecision() throws Exception {
        mockMvc.perform(post("/healing/approve")
                        .contentType("application/json")
                        .content("""
                                {
                                  "recommendationId": "r1",
                                  "decision": "MAYBE",
                                  "approvedBy": "ravi"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("C003"))
                .andExpect(jsonPath("$.detail").value("$.decision: must be one of [\"APPROVED\",\"REJECTED\",\"RESERVED\"]"));
    }

    @Test
    void ingestKnowledge_shouldTriggerIngestion() throws Exception {
        mockMvc.perform(post("/healing/knowledge/ingest"))
                .andExpect(status().isOk());

        verify(ragIngestionService).ingest();
    }
}
