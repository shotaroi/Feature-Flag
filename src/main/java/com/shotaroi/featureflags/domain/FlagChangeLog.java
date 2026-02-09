package com.shotaroi.featureflags.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Audit log entry for feature flag changes (create, update, target add/remove).
 */
@Entity
@Table(name = "flag_change_logs", indexes = {
    @Index(name = "idx_change_log_feature_env", columnList = "feature_key, environment"),
    @Index(name = "idx_change_log_created_at", columnList = "created_at")
})
public class FlagChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_key", nullable = false)
    private String featureKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 50)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 50)
    private FlagChangeType changeType;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    /** Human-readable description of what changed (e.g. "enabled: false -> true", "userId=alice"). */
    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFeatureKey() { return featureKey; }
    public void setFeatureKey(String featureKey) { this.featureKey = featureKey; }

    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }

    public FlagChangeType getChangeType() { return changeType; }
    public void setChangeType(FlagChangeType changeType) { this.changeType = changeType; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
