package com.shotaroi.featureflags.repository;

import com.shotaroi.featureflags.domain.FeatureTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureTargetRepository extends JpaRepository<FeatureTarget, Long> {

    boolean existsByFeatureFlag_IdAndUserId(Long featureFlagId, String userId);

    long deleteByFeatureFlag_IdAndUserId(Long featureFlagId, String userId);
}
