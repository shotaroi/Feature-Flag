package com.shotaroi.featureflags.repository;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FlagChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlagChangeLogRepository extends JpaRepository<FlagChangeLog, Long> {

    List<FlagChangeLog> findByFeatureKeyAndEnvironmentOrderByCreatedAtDesc(String featureKey, Environment environment);
}
