package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.service.FeatureEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flags")
@Tag(name = "Evaluation", description = "Check if a flag is on for a user. Requires X-API-Key header.")
public class FeatureClientController {

    private final FeatureEvaluationService service;

    public FeatureClientController(FeatureEvaluationService service) {
        this.service = service;
    }

    @Operation(summary = "Evaluate a feature flag for a user", description = "Returns enabled (true/false) and reason. Requires X-API-Key.")
    @GetMapping("/{featureKey}/evaluate")
    public FeatureEvaluationService.EvaluationResult evaluate(
            @PathVariable String featureKey,
            @RequestParam Environment environment,
            @RequestParam(required = false) String userId
    ) {
        return service.evaluate(featureKey, environment, userId);
    }
}
