package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.domain.FeatureTarget;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeatureAdminService {

    private final FeatureFlagRepository flagRepo;
    private final FeatureTargetRepository targetRepo;

    public FeatureAdminService(FeatureFlagRepository flagRepo, FeatureTargetRepository targetRepo) {
        this.flagRepo = flagRepo;
        this.targetRepo = targetRepo;
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
    public FeatureFlag create(AdminDtos.CreateFlagRequest req) {
        if (flagRepo.findByFeatureKeyAndEnvironment(req.featureKey(), req.environment()).isPresent()) {
            throw new IllegalArgumentException("Feature already exists: " + req.featureKey() + " in " + req.environment());
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setFeatureKey(req.featureKey());
        flag.setEnvironment(req.environment());
        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @Transactional
    public FeatureFlag update(String featureKey, Environment environment, AdminDtos.UpdateFlagRequest req) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));

        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @Transactional
    public void addTarget(String featureKey, Environment environment, String userId) {
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
    }

    @Transactional
    public void removeTarget(String featureKey, Environment environment, String userId) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey + " in " + environment));

        targetRepo.deleteByFeatureFlag_IdAndUserId(flag.getId(), userId);
    }
}
