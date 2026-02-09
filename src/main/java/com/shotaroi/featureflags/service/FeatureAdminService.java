package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.domain.FlagChangeLog;
import com.shotaroi.featureflags.domain.FlagChangeType;
import com.shotaroi.featureflags.domain.FeatureTarget;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import com.shotaroi.featureflags.repository.FlagChangeLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeatureAdminService {

    private final FeatureFlagRepository flagRepo;
    private final FeatureTargetRepository targetRepo;
    private final FlagChangeLogRepository changeLogRepo;

    public FeatureAdminService(
            FeatureFlagRepository flagRepo,
            FeatureTargetRepository targetRepo,
            FlagChangeLogRepository changeLogRepo
    ) {
        this.flagRepo = flagRepo;
        this.targetRepo = targetRepo;
        this.changeLogRepo = changeLogRepo;
    }

    @Transactional(readOnly = true)
    public FeatureFlag get(String featureKey, Environment environment) {
        return flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> list(Environment environment) {
        return flagRepo.findAllByEnvironment(environment);
    }

    @Transactional
    public FeatureFlag create(AdminDtos.CreateFlagRequest req, String changedBy) {
        if (flagRepo.findByFeatureKeyAndEnvironment(req.featureKey(), req.environment()).isPresent()) {
            throw new IllegalArgumentException("Feature already exists: " + req.featureKey() + " in " + req.environment());
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setFeatureKey(req.featureKey());
        flag.setEnvironment(req.environment());
        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        flag = flagRepo.save(flag);

        logChange(flag.getFeatureKey(), flag.getEnvironment(), FlagChangeType.FLAG_CREATED, changedBy,
                "enabled=" + req.enabled() + ", rolloutPercent=" + req.rolloutPercent());
        return flag;
    }

    @Transactional
    public FeatureFlag update(String featureKey, Environment environment, AdminDtos.UpdateFlagRequest req, String changedBy) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));

        boolean oldEnabled = flag.isEnabled();
        int oldRollout = flag.getRolloutPercent();
        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        flag = flagRepo.save(flag);

        String details = String.format("enabled: %s -> %s, rolloutPercent: %d -> %d", oldEnabled, req.enabled(), oldRollout, req.rolloutPercent());
        logChange(featureKey, environment, FlagChangeType.FLAG_UPDATED, changedBy, details);
        return flag;
    }

    @Transactional
    public void addTarget(String featureKey, Environment environment, String userId, String changedBy) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));

        FeatureTarget target = new FeatureTarget();
        target.setFeatureFlag(flag);
        target.setUserId(userId);

        try {
            targetRepo.save(target);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Target already exists for user: " + userId);
        }
        logChange(featureKey, environment, FlagChangeType.TARGET_ADDED, changedBy, "userId=" + userId);
    }

    @Transactional
    public void removeTarget(String featureKey, Environment environment, String userId, String changedBy) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));

        targetRepo.deleteByFeatureFlag_IdAndUserId(flag.getId(), userId);
        logChange(featureKey, environment, FlagChangeType.TARGET_REMOVED, changedBy, "userId=" + userId);
    }

    @Transactional(readOnly = true)
    public List<FlagChangeLog> listHistory(String featureKey, Environment environment) {
        return changeLogRepo.findByFeatureKeyAndEnvironmentOrderByCreatedAtDesc(featureKey, environment);
    }

    private void logChange(String featureKey, Environment environment, FlagChangeType changeType, String changedBy, String details) {
        FlagChangeLog log = new FlagChangeLog();
        log.setFeatureKey(featureKey);
        log.setEnvironment(environment);
        log.setChangeType(changeType);
        log.setChangedBy(changedBy != null ? changedBy : "system");
        log.setDetails(details);
        changeLogRepo.save(log);
    }
}
