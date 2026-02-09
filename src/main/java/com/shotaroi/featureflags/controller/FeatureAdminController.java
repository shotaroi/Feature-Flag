package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.domain.FlagChangeLog;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.service.FeatureAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/flags")
public class FeatureAdminController {
    private final FeatureAdminService adminService;

    public FeatureAdminController(FeatureAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/{featureKey}")
    public FeatureFlag get(
            @PathVariable String featureKey,
            @RequestParam Environment environment
    ) {
        return adminService.get(featureKey, environment);
    }

    @GetMapping
    public List<FeatureFlag> list(@RequestParam Environment environment) {
        return adminService.list(environment);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlag create(
            @Valid @RequestBody AdminDtos.CreateFlagRequest req,
            Authentication auth
    ) {
        return adminService.create(req, auth != null ? auth.getName() : "anonymous");
    }

    @PatchMapping("/{featureKey}")
    public FeatureFlag update(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @Valid @RequestBody AdminDtos.UpdateFlagRequest req,
            Authentication auth
    ) {
        return adminService.update(featureKey, environment, req, auth != null ? auth.getName() : "anonymous");
    }

    @PostMapping("/{featureKey}/targets")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTarget(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @Valid @RequestBody AdminDtos.AddTargetRequest req,
            Authentication auth
    ) {
        adminService.addTarget(featureKey, environment, req.userId(), auth != null ? auth.getName() : "anonymous");
    }

    @DeleteMapping("/{featureKey}/targets/{userId}")
    public void removeTarget(
            @PathVariable String featureKey,
            @PathVariable String userId,
            @RequestParam Environment environment,
            Authentication auth
    ) {
        adminService.removeTarget(featureKey, environment, userId, auth != null ? auth.getName() : "anonymous");
    }

    @GetMapping("/{featureKey}/history")
    public List<FlagChangeLog> history(
            @PathVariable String featureKey,
            @RequestParam Environment environment
    ) {
        return adminService.listHistory(featureKey, environment);
    }
}
