package com.ravi.eventmind.ai.recommendation.application;

import com.ravi.eventmind.ai.analyzer.SymptomAnalyzerService;
import com.ravi.eventmind.ai.approval.ApprovalRequest;
import com.ravi.eventmind.ai.approval.ApprovalResult;
import com.ravi.eventmind.ai.approval.HumanApprovalService;
import com.ravi.eventmind.ai.jfr.JfrReportClient;
import com.ravi.eventmind.ai.logging.port.LogProvider;
import com.ravi.eventmind.ai.logging.port.LogStore;
import com.ravi.eventmind.ai.model.DiagnosticContext;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import com.ravi.eventmind.ai.rag.retrieval.RagRetrievalService;
import com.ravi.eventmind.ai.recommendation.dto.AnalyzeRequest;
import com.ravi.eventmind.ai.recommendation.port.HealingExecutor;
import com.ravi.eventmind.ai.recommendation.port.HealingRecommendationStore;
import com.ravi.eventmind.shared.correlation.CorrelationId;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The heart of the module. Asks the AI for a recommendation, stores it,
 * and walks it through approval, execution, or rejection.
 */
@Slf4j
@Service
public class HealingRecommendationService {

    private static final int DEFAULT_TOP_K = 3;
    private static final int LOG_FETCH_LIMIT = 20;

    private final SymptomAnalyzerService symptomAnalyzerService;
    private final RagRetrievalService ragRetrievalService;
    private final HealingRecommendationStore store;
    private final HumanApprovalService humanApprovalService;
    private final HealingExecutor healingExecutor;
    private final LogProvider logProvider;
    private final LogStore logStore;
    private final JfrReportClient jfrReportClient;

    public HealingRecommendationService(SymptomAnalyzerService symptomAnalyzerService,
                                        RagRetrievalService ragRetrievalService,
                                        HealingRecommendationStore store,
                                        HumanApprovalService humanApprovalService,
                                        HealingExecutor healingExecutor,
                                        LogProvider logProvider,
                                        LogStore logStore,
                                        JfrReportClient jfrReportClient) {
        this.symptomAnalyzerService = symptomAnalyzerService;
        this.ragRetrievalService = ragRetrievalService;
        this.store = store;
        this.humanApprovalService = humanApprovalService;
        this.healingExecutor = healingExecutor;
        this.logProvider = logProvider;
        this.logStore = logStore;
        this.jfrReportClient = jfrReportClient;
    }

    public HealingRecommendation analyze(AnalyzeRequest request) {
        String symptom = request.symptom();
        if (symptom == null || symptom.isBlank()) {
            throw new EventMindExceptions(EventMindReasonsEnum.SYMPTOM_INVALID,
                    "symptom is required for analysis");
        }

        String knowledge = ragRetrievalService.search(symptom, topK(request))
                .stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        DiagnosticContext context = new DiagnosticContext(symptom, resolveLogs(), resolveJfrReport(request));
        HealingRecommendation recommendation = symptomAnalyzerService.analyze(context, knowledge);

        String id = UUID.randomUUID().toString();
        HealingRecommendation created = recommendation
                .withRecommendationId(id)
                .withSymptomId(request.symptomId())
                .withStatus(RecommendationStatus.PENDING_APPROVAL);
        store.save(created);
        log.info("Created recommendation {} with status PENDING_APPROVAL", id);
        logStore.save("INFO", String.format("Created recommendation %s with status PENDING_APPROVAL", id), null,
                CorrelationId.get());
        return created;
    }

    public HealingRecommendation findById(String recommendationId) {
        return store.findById(recommendationId)
                .orElseThrow(() -> new EventMindExceptions(
                        EventMindReasonsEnum.RECOMMENDATION_NOT_FOUND,
                        "Recommendation not found: " + recommendationId));
    }

    public ApprovalResult approve(ApprovalRequest request) {
        HealingRecommendation recommendation = findById(request.recommendationId());
        requirePending(recommendation);
        requireHumanApproval(recommendation);

        ApprovalResult result = humanApprovalService.processApproval(recommendation, request);

        if (result.approved()) {
            return approveAndExecute(recommendation, result);
        }
        return reject(recommendation, result);
    }

    private ApprovalResult approveAndExecute(HealingRecommendation recommendation, ApprovalResult result) {
        if (!store.transition(recommendation.recommendationId(),
                RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.APPROVED)) {
            throw new EventMindExceptions(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT,
                    "Recommendation " + recommendation.recommendationId() + " is no longer pending approval");
        }
        try {
            healingExecutor.execute(recommendation);
            store.transition(recommendation.recommendationId(),
                    RecommendationStatus.APPROVED, RecommendationStatus.EXECUTED);
            logStore.save("INFO", String.format("Executed recommendation %s", recommendation.recommendationId()), null,
                    CorrelationId.get());
        } catch (Exception e) {
            store.transition(recommendation.recommendationId(),
                    RecommendationStatus.APPROVED, RecommendationStatus.FAILED);
            log.error("Execution failed for recommendation {}", recommendation.recommendationId(), e);
            logStore.save("ERROR",
                    String.format("Execution failed for recommendation %s", recommendation.recommendationId()),
                    e.getMessage(),
                    CorrelationId.get());
            throw new EventMindExceptions(EventMindReasonsEnum.EXECUTION_FAILED, "Execution failed", e);
        }
        return result;
    }

    private ApprovalResult reject(HealingRecommendation recommendation, ApprovalResult result) {
        if (!store.transition(recommendation.recommendationId(),
                RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.REJECTED)) {
            throw new EventMindExceptions(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT,
                    "Recommendation " + recommendation.recommendationId() + " is no longer pending approval");
        }
        logStore.save("INFO", String.format("Recommendation %s rejected", recommendation.recommendationId()), null,
                CorrelationId.get());
        return result;
    }

    private void requirePending(HealingRecommendation recommendation) {
        if (recommendation.status() != RecommendationStatus.PENDING_APPROVAL) {
            throw new EventMindExceptions(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT,
                    "Recommendation " + recommendation.recommendationId()
                            + " is not pending approval (current status: " + recommendation.status() + ")");
        }
    }

    private void requireHumanApproval(HealingRecommendation recommendation) {
        if (!recommendation.requiresHumanApproval()) {
            throw new EventMindExceptions(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT,
                    "Recommendation " + recommendation.recommendationId() + " does not require approval");
        }
    }

    private int topK(AnalyzeRequest request) {
        return request.topK() != null && request.topK() > 0 ? request.topK() : DEFAULT_TOP_K;
    }

    private String resolveLogs() {
        return logProvider.recentLogs(LOG_FETCH_LIMIT).stream()
                .map(log -> String.format("[%s] %s", log.level(), log.message()))
                .collect(Collectors.joining("\n"));
    }

    private String resolveJfrReport(AnalyzeRequest request) {
        if (request.jfrReport() != null && !request.jfrReport().isBlank()) {
            return request.jfrReport();
        }
        return jfrReportClient.fetch();
    }
}
