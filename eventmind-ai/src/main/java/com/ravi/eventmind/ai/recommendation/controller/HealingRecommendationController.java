package com.ravi.eventmind.ai.recommendation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.approval.ApprovalRequest;
import com.ravi.eventmind.ai.approval.ApprovalResult;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.rag.ingestion.RagIngestionService;
import com.ravi.eventmind.ai.recommendation.application.HealingRecommendationService;
import com.ravi.eventmind.ai.recommendation.dto.AnalyzeRequest;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import com.ravi.eventmind.shared.validation.JsonSchemaValidator;
import com.ravi.eventmind.shared.validation.SchemaValidation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Takes HTTP calls, hands them over to the service, and sends back whatever comes out.
 * Also has a small ingest endpoint to refresh the knowledge base. No business logic lives here.
 */
@RestController
@RequestMapping("/healing")
public class HealingRecommendationController {

    private final HealingRecommendationService healingRecommendationService;
    private final RagIngestionService ragIngestionService;
    private final JsonSchemaValidator analyzeRequestSchemaValidator;
    private final JsonSchemaValidator approvalRequestSchemaValidator;
    private final ObjectMapper objectMapper;

    public HealingRecommendationController(HealingRecommendationService healingRecommendationService,
                                           RagIngestionService ragIngestionService,
                                           @Qualifier("analyzeRequestSchemaValidator") JsonSchemaValidator analyzeRequestSchemaValidator,
                                           @Qualifier("approvalRequestSchemaValidator") JsonSchemaValidator approvalRequestSchemaValidator,
                                           ObjectMapper objectMapper) {
        this.healingRecommendationService = healingRecommendationService;
        this.ragIngestionService = ragIngestionService;
        this.analyzeRequestSchemaValidator = analyzeRequestSchemaValidator;
        this.approvalRequestSchemaValidator = approvalRequestSchemaValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/analyze")
    public HealingRecommendation analyze(@RequestBody JsonNode body) {
        SchemaValidation.requireValid(analyzeRequestSchemaValidator, body);
        AnalyzeRequest request = toValue(body, AnalyzeRequest.class);
        return healingRecommendationService.analyze(request);
    }

    @GetMapping("/recommendations/{id}")
    public HealingRecommendation getRecommendation(@PathVariable String id) {
        return healingRecommendationService.findById(id);
    }

    @PostMapping("/approve")
    public ApprovalResult approve(@RequestBody JsonNode body) {
        SchemaValidation.requireValid(approvalRequestSchemaValidator, body);
        ApprovalRequest request = toValue(body, ApprovalRequest.class);
        return healingRecommendationService.approve(request);
    }

    @PostMapping("/knowledge/ingest")
    public void ingestKnowledge() {
        ragIngestionService.ingest();
    }

    private <T> T toValue(JsonNode body, Class<T> type) {
        try {
            return objectMapper.treeToValue(body, type);
        } catch (JsonProcessingException e) {
            throw new EventMindExceptions(
                    EventMindReasonsEnum.INVALID_REQUEST_BODY,
                    "Request body could not be mapped: " + e.getOriginalMessage(),
                    e
            );
        }
    }
}
