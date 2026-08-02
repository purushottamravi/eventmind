package com.ravi.eventmind.ai.recommendation;

import com.ravi.eventmind.ai.model.RecommendationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "HEALING_RECOMMENDATION")
@Data
@NoArgsConstructor
public class HealingRecommendationEntity {

    @Id
    private String recommendationId;

    private String symptomId;

    private String problem;

    private String severity;

    private String rootCause;

    @Lob
    private String evidence;

    private String suggestedAction;

    private Integer confidence;

    private boolean requiresHumanApproval;

    @Enumerated(EnumType.STRING)
    private RecommendationStatus status;

    @Version
    private long version;

    private LocalDateTime createdAt;
}
