package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.service.FeatureAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public FeatureFlag create(@Valid @RequestBody AdminDtos.CreateFlagRequest req) {
        return adminService.create(req);
    }

    @PatchMapping("/{featureKey}")
    public FeatureFlag update(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @Valid @RequestBody AdminDtos.UpdateFlagRequest req
    ) {
        return adminService.update(featureKey, environment, req);
    }

    @PostMapping("/{featureKey}/targets")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTarget(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @Valid @RequestBody AdminDtos.AddTargetRequest req
    ) {
        adminService.addTarget(featureKey, environment, req.userId());
    }

    @DeleteMapping("/{featureKey}/targets/{userId}")
    public void removeTarget(
            @PathVariable String featureKey,
            @PathVariable String userId,
            @RequestParam Environment environment
    ) {
        adminService.removeTarget(featureKey, environment, userId);
    }
}
