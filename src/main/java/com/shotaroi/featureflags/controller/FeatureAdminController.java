package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.domain.FeatureTarget;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import com.shotaroi.featureflags.service.FeatureAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/flags")
public class FeatureAdminController {
    private final FeatureAdminService adminService;

    public FeatureAdminController(
            FeatureAdminService adminService
    ) {
        this.adminService = adminService;
    }

    @GetMapping("/{featureKey}")
    public FeatureFlag get(@PathVariable String featureKey) {
        return adminService.get(featureKey);
    }

    @GetMapping
    public java.util.List<FeatureFlag> list() {
        return adminService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlag create(@Valid @RequestBody AdminDtos.CreateFlagRequest req) {
        return adminService.create(req);
    }

    @PatchMapping("/{featureKey}")
    public FeatureFlag update(
            @PathVariable String featureKey,
            @Valid @RequestBody AdminDtos.UpdateFlagRequest req
    ) {
        return adminService.update(featureKey, req);
    }

    @PostMapping("/{featureKey}/targets")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTarget(
            @PathVariable String featureKey,
            @Valid @RequestBody AdminDtos.AddTargetRequest req
    ) {
        adminService.addTarget(featureKey, req.userId());
    }

    @DeleteMapping("/{featureKey}/targets/{userId}")
    @Transactional
    public void removeTarget(@PathVariable String featureKey, @PathVariable String userId) {
        adminService.removeTarget(featureKey, userId);
    }
}
