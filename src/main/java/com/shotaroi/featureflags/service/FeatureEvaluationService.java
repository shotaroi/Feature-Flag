package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class FeatureEvaluationService {

    private static final String METRIC_EVALUATIONS = "feature.flag.evaluations";
    private static final String METRIC_EVALUATION_DURATION = "feature.flag.evaluation.duration";

    private final FeatureFlagRepository flagRepo;
    private final FeatureTargetRepository targetRepo;
    private final MeterRegistry meterRegistry;

    public FeatureEvaluationService(
            FeatureFlagRepository flagRepo,
            FeatureTargetRepository targetRepo,
            MeterRegistry meterRegistry
    ) {
        this.flagRepo = flagRepo;
        this.targetRepo = targetRepo;
        this.meterRegistry = meterRegistry;
    }

    public EvaluationResult evaluate(String featureKey, Environment environment, String userId) {
        return Timer.builder(METRIC_EVALUATION_DURATION)
                .tag("feature_key", featureKey)
                .tag("environment", environment.name())
                .register(meterRegistry)
                .record(() -> doEvaluate(featureKey, environment, userId));
    }

    private EvaluationResult doEvaluate(String featureKey, Environment environment, String userId) {
        FeatureFlag flag = flagRepo.findByFeatureKeyAndEnvironment(featureKey, environment).orElse(null);

        if (flag == null) {
            recordEvaluation(featureKey, environment, false, "FLAG_NOT_FOUND");
            return EvaluationResult.off("FLAG_NOT_FOUND");
        }

        if (!flag.isEnabled()) {
            recordEvaluation(featureKey, environment, false, "FLAG_DISABLED");
            return EvaluationResult.off("FLAG_DISABLED");
        }

        if (userId != null && !userId.isBlank()) {
            boolean targeted =
                    targetRepo.existsByFeatureFlag_IdAndUserId(flag.getId(), userId);
            if (targeted) {
                recordEvaluation(featureKey, environment, true, "TARGETED_USER");
                return EvaluationResult.on("TARGETED_USER");
            }
        }

        int rollout = flag.getRolloutPercent();
        if (rollout <= 0) {
            recordEvaluation(featureKey, environment, false, "ROLLOUT_0");
            return EvaluationResult.off("ROLLOUT_0");
        }
        if (rollout >= 100) {
            recordEvaluation(featureKey, environment, true, "ROLLOUT_100");
            return EvaluationResult.on("ROLLOUT_100");
        }

        int bucket = stableBucket(featureKey, userId);
        boolean on = bucket < rollout;
        String reason = "ROLLOUT_BUCKET_" + bucket;
        recordEvaluation(featureKey, environment, on, reason);
        return on ? EvaluationResult.on(reason) : EvaluationResult.off(reason);
    }

    private void recordEvaluation(String featureKey, Environment environment, boolean enabled, String reason) {
        Counter.builder(METRIC_EVALUATIONS)
                .tag("feature_key", featureKey)
                .tag("environment", environment.name())
                .tag("result", enabled ? "on" : "off")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    int stableBucket(String featureKey, String userId) {
        try {
            String input = featureKey + ":" + (userId == null ? "" : userId);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return (hash[0] & 0xFF) % 100;
        } catch (Exception e) {
            return Math.abs((featureKey + userId).hashCode()) % 100;
        }
    }

    public record EvaluationResult(boolean enabled, String reason) {
        static EvaluationResult on(String r) { return new EvaluationResult(true, r); }
        static EvaluationResult off(String r) { return new EvaluationResult(false, r); }
    }
}
