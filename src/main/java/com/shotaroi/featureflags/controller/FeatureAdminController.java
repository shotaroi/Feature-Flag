package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.domain.FeatureTarget;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flags")
public class FeatureAdminController {

    private final FeatureFlagRepository flagRepo;
    private final FeatureTargetRepository targetRepo;

    public FeatureAdminController(
            FeatureFlagRepository flagRepo,
            FeatureTargetRepository targetRepo
    ) {
        this.flagRepo = flagRepo;
        this.targetRepo = targetRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlag create(@Valid @RequestBody AdminDtos.CreateFlagRequest req) {
        if (flagRepo.findByFeatureKey(req.featureKey()).isPresent()) {
            throw new IllegalArgumentException("Feature already exists");
        }

        FeatureFlag flag = new FeatureFlag();
        flag.setFeatureKey(req.featureKey());
        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @PatchMapping("/{featureKey}")
    public FeatureFlag update(
            @PathVariable String featureKey,
            @Valid @RequestBody AdminDtos.UpdateFlagRequest req
    ) {
        FeatureFlag flag = flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        flag.setEnabled(req.enabled());
        flag.setRolloutPercent(req.rolloutPercent());
        return flagRepo.save(flag);
    }

    @PostMapping("/{featureKey}/targets")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTarget(
            @PathVariable String featureKey,
            @Valid @RequestBody AdminDtos.AddTargetRequest req
    ) {
        FeatureFlag flag = flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));

        FeatureTarget target = new FeatureTarget();
        target.setFeatureFlag(flag);
        target.setUserId(req.userId());
        targetRepo.save(target);
    }
}
