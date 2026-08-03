package com.ravi.eventmind.ai.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ravi.eventmind.ai.model.Evidence;
import com.ravi.eventmind.ai.model.RecommendationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The JPA-backed implementation of the recommendation store.
 * Converts between the model and the entity (evidence lives in the DB as JSON).
 */
@Service
public class JpaHealingRecommendationStore implements HealingRecommendationStore {

    private final HealingRecommendationRepository repository;
    private final ObjectMapper objectMapper;

    public JpaHealingRecommendationStore(HealingRecommendationRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public com.ravi.eventmind.ai.model.HealingRecommendation save(
            com.ravi.eventmind.ai.model.HealingRecommendation recommendation) {
        repository.save(toEntity(recommendation));
        return recommendation;
    }

    @Override
    public Optional<com.ravi.eventmind.ai.model.HealingRecommendation> findById(String recommendationId) {
        return repository.findById(recommendationId).map(this::toModel);
    }

    @Override
    @Transactional
    public boolean transition(String recommendationId, RecommendationStatus from, RecommendationStatus to) {
        return repository.transitionStatus(recommendationId, from, to) > 0;
    }

    private HealingRecommendation toEntity(com.ravi.eventmind.ai.model.HealingRecommendation recommendation) {
        HealingRecommendation entity = new HealingRecommendation();
        entity.setRecommendationId(recommendation.recommendationId());
        entity.setSymptomId(recommendation.symptomId());
        entity.setProblem(recommendation.problem());
        entity.setSeverity(recommendation.severity());
        entity.setRootCause(recommendation.rootCause());
        entity.setEvidence(toJson(recommendation.evidence()));
        entity.setSuggestedAction(recommendation.suggestedAction());
        entity.setConfidence(recommendation.confidence());
        entity.setRequiresHumanApproval(recommendation.requiresHumanApproval());
        entity.setStatus(recommendation.status());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private com.ravi.eventmind.ai.model.HealingRecommendation toModel(HealingRecommendation entity) {
        return new com.ravi.eventmind.ai.model.HealingRecommendation(
                entity.getRecommendationId(),
                entity.getSymptomId(),
                entity.getProblem(),
                entity.getSeverity(),
                entity.getRootCause(),
                fromJson(entity.getEvidence()),
                entity.getSuggestedAction(),
                entity.getConfidence(),
                entity.isRequiresHumanApproval(),
                entity.getStatus());
    }

    private String toJson(List<Evidence> evidence) {
        if (evidence == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<Evidence> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Evidence>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
