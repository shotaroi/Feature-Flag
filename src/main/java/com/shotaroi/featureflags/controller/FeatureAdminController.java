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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/flags")
@Tag(name = "Admin – Feature flags", description = "CRUD and targets. Requires HTTP Basic (admin).")
public class FeatureAdminController {
    private final FeatureAdminService adminService;

    public FeatureAdminController(FeatureAdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Get a flag by key and environment")
    @GetMapping("/{featureKey}")
    public FeatureFlag get(
            @PathVariable String featureKey,
            @RequestParam Environment environment
    ) {
        return adminService.get(featureKey, environment);
    }

    @Operation(summary = "List all flags for an environment")
    @GetMapping
    public List<FeatureFlag> list(@RequestParam Environment environment) {
        return adminService.list(environment);
    }

    @Operation(summary = "Create a new flag")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlag create(
            @Valid @RequestBody AdminDtos.CreateFlagRequest req,
            Authentication auth
    ) {
        return adminService.create(req, auth != null ? auth.getName() : "anonymous");
    }

    @Operation(summary = "Update a flag (enabled, rolloutPercent)")
    @PatchMapping("/{featureKey}")
    public FeatureFlag update(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @Valid @RequestBody AdminDtos.UpdateFlagRequest req,
            Authentication auth
    ) {
        return adminService.update(featureKey, environment, req, auth != null ? auth.getName() : "anonymous");
    }

    @Operation(summary = "Add a user to the flag allowlist")
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

    @Operation(summary = "Remove a user from the flag allowlist")
    @DeleteMapping("/{featureKey}/targets/{userId}")
    public void removeTarget(
            @PathVariable String featureKey,
            @PathVariable String userId,
            @RequestParam Environment environment,
            Authentication auth
    ) {
        adminService.removeTarget(featureKey, environment, userId, auth != null ? auth.getName() : "anonymous");
    }

    @Operation(summary = "Get change history for a flag")
    @GetMapping("/{featureKey}/history")
    public List<FlagChangeLog> history(
            @PathVariable String featureKey,
            @RequestParam Environment environment
    ) {
        return adminService.listHistory(featureKey, environment);
    }
}
