package com.shotaroi.featureflags.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "feature_flags",
        uniqueConstraints = @UniqueConstraint(
            name = "uk_feature_key",
            columnNames = "feature_key"
        )
)
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_key", nullable = false)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "rollout_percent", nullable = false)
    private int rolloutPercent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFeatureKey() { return featureKey; }
    public void setFeatureKey(String featureKey) { this.featureKey = featureKey; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getRolloutPercent() { return rolloutPercent; }
    public void setRolloutPercent(int rolloutPercent) { this.rolloutPercent = rolloutPercent; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
