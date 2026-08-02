package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.analyzer.SymptomAnalyzerService;
import com.ravi.eventmind.ai.approval.ApprovalDecision;
import com.ravi.eventmind.ai.approval.ApprovalRequest;
import com.ravi.eventmind.ai.approval.ApprovalResult;
import com.ravi.eventmind.ai.approval.HumanApprovalService;
import com.ravi.eventmind.ai.jfr.JfrReportClient;
import com.ravi.eventmind.ai.logging.LogProvider;
import com.ravi.eventmind.ai.logging.LogStore;
import com.ravi.eventmind.ai.model.DiagnosticContext;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import com.ravi.eventmind.ai.rag.retrieval.RagRetrievalService;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the analyze and approve flows, including rejections, conflicts, and execution failures.
 */
@ExtendWith(MockitoExtension.class)
class HealingRecommendationServiceTest {

    @Mock
    private SymptomAnalyzerService symptomAnalyzerService;

    @Mock
    private RagRetrievalService ragRetrievalService;

    @Mock
    private HealingRecommendationStore store;

    @Mock
    private HumanApprovalService humanApprovalService;

    @Mock
    private HealingExecutor healingExecutor;

    @Mock
    private LogProvider logProvider;

    @Mock
    private LogStore logStore;

    @Mock
    private JfrReportClient jfrReportClient;

    private HealingRecommendationService service() {
        return new HealingRecommendationService(symptomAnalyzerService, ragRetrievalService,
                store, humanApprovalService, healingExecutor, logProvider, logStore, jfrReportClient);
    }

    @Test
    void analyze_shouldEnrichWithRagStampAndPersist() {
        when(ragRetrievalService.search("OOM", 3))
                .thenReturn(List.of(new Document("Heap exhaustion causes OOM")));
        HealingRecommendation llmResult = new HealingRecommendation(null, null, "OutOfMemoryError",
                "HIGH", "heap", List.of(), "RESTART_SERVICE", 90, true, null);
        when(symptomAnalyzerService.analyze(any(DiagnosticContext.class), eq("Heap exhaustion causes OOM")))
                .thenReturn(llmResult);

        HealingRecommendation result = service().analyze(new AnalyzeRequest("s1", "OOM", null, "jfr", 3));

        assertNotNull(result.recommendationId());
        assertEquals("s1", result.symptomId());
        assertEquals(RecommendationStatus.PENDING_APPROVAL, result.status());
        verify(store).save(result);
        verify(logStore).save(eq("INFO"), any(String.class), eq(null), any());
    }

    @Test
    void analyze_shouldRejectBlankSymptom() {
        assertThrows(IllegalArgumentException.class,
                () -> service().analyze(new AnalyzeRequest(null, "   ", null, null, null)));
    }

    @Test
    void analyze_shouldAutoFetchJfrReportWhenNotProvided() {
        when(ragRetrievalService.search("OOM", 3)).thenReturn(List.of());
        when(jfrReportClient.fetch()).thenReturn("JFR REPORT (generated 2026-01-01T00:00:00Z)\n");
        HealingRecommendation llmResult = new HealingRecommendation(null, null, "OutOfMemoryError",
                "HIGH", "heap", List.of(), "RESTART_SERVICE", 90, true, null);
        when(symptomAnalyzerService.analyze(any(DiagnosticContext.class), any()))
                .thenReturn(llmResult);

        HealingRecommendation result = service().analyze(new AnalyzeRequest("s1", "OOM", null, null, 3));

        verify(jfrReportClient).fetch();
        ArgumentCaptor<DiagnosticContext> contextCaptor = ArgumentCaptor.forClass(DiagnosticContext.class);
        verify(symptomAnalyzerService).analyze(contextCaptor.capture(), any());
        assertEquals("JFR REPORT (generated 2026-01-01T00:00:00Z)\n", contextCaptor.getValue().jfrReport());
        assertEquals(RecommendationStatus.PENDING_APPROVAL, result.status());
    }

    @Test
    void approve_shouldExecuteWhenApproved() {
        HealingRecommendation recommendation = recommendation();
        when(store.findById("r1")).thenReturn(Optional.of(recommendation));
        when(humanApprovalService.processApproval(eq(recommendation), any(ApprovalRequest.class)))
                .thenReturn(new ApprovalResult("r1", true, "ravi", "go"));
        when(store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.APPROVED))
                .thenReturn(true);
        when(store.transition("r1", RecommendationStatus.APPROVED, RecommendationStatus.EXECUTED))
                .thenReturn(true);

        ApprovalResult result = service().approve(new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go"));

        assertTrue(result.approved());
        verify(healingExecutor).execute(recommendation);
        verify(store).transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.APPROVED);
        verify(store).transition("r1", RecommendationStatus.APPROVED, RecommendationStatus.EXECUTED);
    }

    @Test
    void approve_shouldRejectWithoutExecuting() {
        HealingRecommendation recommendation = recommendation();
        when(store.findById("r1")).thenReturn(Optional.of(recommendation));
        when(humanApprovalService.processApproval(eq(recommendation), any(ApprovalRequest.class)))
                .thenReturn(new ApprovalResult("r1", false, "ravi", "no"));
        when(store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.REJECTED))
                .thenReturn(true);

        ApprovalResult result = service().approve(new ApprovalRequest("r1", ApprovalDecision.REJECTED, "ravi", "no"));

        assertTrue(!result.approved());
        verifyNoInteractions(healingExecutor);
        verify(store).transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.REJECTED);
    }

    @Test
    void approve_shouldMarkFailedWhenExecutionThrows() {
        HealingRecommendation recommendation = recommendation();
        when(store.findById("r1")).thenReturn(Optional.of(recommendation));
        when(humanApprovalService.processApproval(eq(recommendation), any(ApprovalRequest.class)))
                .thenReturn(new ApprovalResult("r1", true, "ravi", "go"));
        when(store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.APPROVED))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("cannot restart"))
                .when(healingExecutor).execute(recommendation);

        EventMindExceptions exception = assertThrows(EventMindExceptions.class,
                () -> service().approve(new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go")));

        assertEquals(EventMindReasonsEnum.EXECUTION_FAILED, exception.getReason());
        verify(store).transition("r1", RecommendationStatus.APPROVED, RecommendationStatus.FAILED);
    }

    @Test
    void approve_shouldConflictWhenNotPending() {
        HealingRecommendation executed = recommendation()
                .withStatus(RecommendationStatus.EXECUTED);
        when(store.findById("r1")).thenReturn(Optional.of(executed));

        EventMindExceptions exception = assertThrows(EventMindExceptions.class,
                () -> service().approve(new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go")));

        assertEquals(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT, exception.getReason());
        verifyNoInteractions(humanApprovalService, healingExecutor);
    }

    @Test
    void approve_shouldConflictWhenApprovalNotRequired() {
        HealingRecommendation auto = new HealingRecommendation("r1", "s1", "OOM", "HIGH",
                "heap", List.of(), "RESTART_SERVICE", 90, false, RecommendationStatus.PENDING_APPROVAL);
        when(store.findById("r1")).thenReturn(Optional.of(auto));

        EventMindExceptions exception = assertThrows(EventMindExceptions.class,
                () -> service().approve(new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go")));

        assertEquals(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT, exception.getReason());
        verifyNoInteractions(humanApprovalService, healingExecutor);
    }

    @Test
    void approve_shouldConflictWhenTransitionLostToConcurrentRequest() {
        HealingRecommendation recommendation = recommendation();
        when(store.findById("r1")).thenReturn(Optional.of(recommendation));
        when(humanApprovalService.processApproval(eq(recommendation), any(ApprovalRequest.class)))
                .thenReturn(new ApprovalResult("r1", true, "ravi", "go"));
        when(store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.APPROVED))
                .thenReturn(false);

        EventMindExceptions exception = assertThrows(EventMindExceptions.class,
                () -> service().approve(new ApprovalRequest("r1", ApprovalDecision.APPROVED, "ravi", "go")));

        assertEquals(EventMindReasonsEnum.RECOMMENDATION_STATE_CONFLICT, exception.getReason());
        verifyNoInteractions(healingExecutor);
    }

    @Test
    void approve_shouldReturnNotFoundWhenMissing() {
        when(store.findById("missing")).thenReturn(Optional.empty());

        EventMindExceptions exception = assertThrows(EventMindExceptions.class,
                () -> service().approve(new ApprovalRequest("missing", ApprovalDecision.APPROVED, "ravi", "go")));

        assertEquals(EventMindReasonsEnum.RECOMMENDATION_NOT_FOUND, exception.getReason());
        verify(healingExecutor, never()).execute(any());
    }

    private HealingRecommendation recommendation() {
        return new HealingRecommendation("r1", "s1", "OOM", "HIGH", "heap",
                List.of(), "RESTART_SERVICE", 90, true, RecommendationStatus.PENDING_APPROVAL);
    }
}
