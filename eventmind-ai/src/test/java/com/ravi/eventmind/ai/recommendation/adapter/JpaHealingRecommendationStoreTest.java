package com.ravi.eventmind.ai.recommendation.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.model.Evidence;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import com.ravi.eventmind.ai.recommendation.entity.HealingRecommendationEntity;
import com.ravi.eventmind.ai.recommendation.repository.HealingRecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Proves the store saves, reads, and transitions recommendations with the repository.
 */
@ExtendWith(MockitoExtension.class)
class JpaHealingRecommendationStoreTest {

    @Mock
    private HealingRecommendationRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void save_shouldPersistEntityWithEvidenceAsJson() {
        HealingRecommendation recommendation =
                new HealingRecommendation("r1", "s1", "OOM", "HIGH",
                "heap", List.of(new Evidence("LOG", "x", "HIGH")), "RESTART_SERVICE", 90, true,
                RecommendationStatus.PENDING_APPROVAL);

        JpaHealingRecommendationStore store = new JpaHealingRecommendationStore(repository, objectMapper);
        store.save(recommendation);

        ArgumentCaptor<HealingRecommendationEntity> captor = ArgumentCaptor.forClass(HealingRecommendationEntity.class);
        verify(repository).save(captor.capture());
        assertEquals("r1", captor.getValue().getRecommendationId());
        assertEquals(RecommendationStatus.PENDING_APPROVAL, captor.getValue().getStatus());
        assertTrue(captor.getValue().getEvidence().contains("\"LOG\""));
    }

    @Test
    void findById_shouldMapEntityBackToModel() {
        HealingRecommendationEntity entity = new HealingRecommendationEntity();
        entity.setRecommendationId("r1");
        entity.setSymptomId("s1");
        entity.setProblem("OOM");
        entity.setSeverity("HIGH");
        entity.setRootCause("heap");
        entity.setEvidence("[{\"source\":\"LOG\",\"finding\":\"x\",\"severity\":\"HIGH\"}]");
        entity.setSuggestedAction("RESTART_SERVICE");
        entity.setConfidence(90);
        entity.setRequiresHumanApproval(true);
        entity.setStatus(RecommendationStatus.PENDING_APPROVAL);
        when(repository.findById("r1")).thenReturn(Optional.of(entity));

        JpaHealingRecommendationStore store = new JpaHealingRecommendationStore(repository, objectMapper);
        Optional<HealingRecommendation> result = store.findById("r1");

        assertTrue(result.isPresent());
        assertEquals("r1", result.get().recommendationId());
        assertEquals(1, result.get().evidence().size());
        assertEquals("LOG", result.get().evidence().get(0).source());
    }

    @Test
    void transition_shouldIssueAtomicGuardedUpdate() {
        JpaHealingRecommendationStore store = new JpaHealingRecommendationStore(repository, objectMapper);
        when(repository.transitionStatus("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.EXECUTED))
                .thenReturn(1);

        boolean updated = store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.EXECUTED);

        assertTrue(updated);
    }

    @Test
    void transition_shouldReturnFalseWhenNoRowInExpectedState() {
        JpaHealingRecommendationStore store = new JpaHealingRecommendationStore(repository, objectMapper);
        when(repository.transitionStatus("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.EXECUTED))
                .thenReturn(0);

        boolean updated = store.transition("r1", RecommendationStatus.PENDING_APPROVAL, RecommendationStatus.EXECUTED);

        assertFalse(updated);
    }
}
