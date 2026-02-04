package com.shotaroi.featureflags.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feature_flags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feature_key",
                columnNames = "featureKey"
        ))
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int rolloutPercent;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
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
    public String getFeatureKey() { return featureKey; }
    public void setFeatureKey(String featureKey) { this.featureKey = featureKey; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getRolloutPercent() { return rolloutPercent; }
    public void setRolloutPercent(int rolloutPercent) { this.rolloutPercent = rolloutPercent; }
}
