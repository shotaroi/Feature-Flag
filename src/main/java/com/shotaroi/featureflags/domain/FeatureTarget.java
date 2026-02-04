package com.shotaroi.featureflags.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "feature_targets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feature_user",
                columnNames = {"featureFlag_id", "userId"}
        ))
public class FeatureTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private FeatureFlag featureFlag;

    @Column(nullable = false)
    private String userId;

    public Long getId() { return id; }
    public FeatureFlag getFeatureFlag() { return featureFlag; }
    public void setFeatureFlag(FeatureFlag featureFlag) { this.featureFlag = featureFlag; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
