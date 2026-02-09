package com.shotaroi.featureflags.repository;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    Optional<FeatureFlag> findByFeatureKeyAndEnvironment(String featureKey, Environment environment);

    List<FeatureFlag> findAllByEnvironment(Environment environment);
}
