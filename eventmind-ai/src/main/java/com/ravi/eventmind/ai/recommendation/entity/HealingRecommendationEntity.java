package com.ravi.eventmind.ai.recommendation.entity;

import com.ravi.eventmind.ai.model.RecommendationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * A row in the recommendations table, one saved recommendation per row.
 * Plain JPA, used mostly by the store when moving data in and out of the DB.
 */
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

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
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
