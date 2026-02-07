package com.shotaroi.featureflags.service;

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
    public FeatureFlag get(String featureKey) {
        return flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey));
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> list() {
        return flagRepo.findAll();
    }

    @Transactional
    public FeatureFlag create(AdminDtos.CreateFlagRequest req) {
        if (flagRepo.findByFeatureKey(req.featureKey()).isPresent()) {
            throw new IllegalArgumentException("Feature already exists: " + req.featureKey());
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setFeatureKey(req.featureKey());
        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @Transactional
    public FeatureFlag update(String featureKey, AdminDtos.UpdateFlagRequest req) {
        FeatureFlag flag = flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey));

        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @Transactional
    public void addTarget(String featureKey, String userId) {
        FeatureFlag flag = flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey));

        FeatureTarget target = new FeatureTarget();
        target.setFeatureFlag(flag);
        target.setUserId(userId);

        try {
            targetRepo.save(target);
        } catch (DataIntegrityViolationException e) {
            // Most likely your unique constraint (featureFlag_id + userId) was violated
            throw new IllegalArgumentException("Target already exists for user: " + userId);
        }
    }

    @Transactional
    public void removeTarget(String featureKey, String userId) {
        var flag = flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + featureKey));

        targetRepo.deleteByFeatureFlag_IdAndUserId(flag.getId(), userId);
    }
}
