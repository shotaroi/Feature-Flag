package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.service.FeatureEvaluationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flags")
public class FeatureClientController {

    private final FeatureEvaluationService service;

    public FeatureClientController(FeatureEvaluationService service) {
        this.service = service;
    }

    @GetMapping("/{featureKey}/evaluate")
    public FeatureEvaluationService.EvaluationResult evaluate(
            @PathVariable String featureKey,
            @RequestParam(required = false) String userId
    ) {
        return service.evaluate(featureKey, userId);
    }
}
