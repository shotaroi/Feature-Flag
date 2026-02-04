package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class FeatureEvaluationService {

    private final FeatureFlagRepository flagRepo;
    private final FeatureTargetRepository targetRepo;

    public FeatureEvaluationService(
            FeatureFlagRepository flagRepo,
            FeatureTargetRepository targetRepo
    ) {
        this.flagRepo = flagRepo;
        this.targetRepo = targetRepo;
    }

    public EvaluationResult evaluate(String featureKey, String userId) {
        FeatureFlag flag = flagRepo.findByFeatureKey(featureKey).orElse(null);

        if (flag == null) {
            return EvaluationResult.off("FLAG_NOT_FOUND");
        }

        if (!flag.isEnabled()) {
            return EvaluationResult.off("FLAG_DISABLED");
        }

        if (userId != null && !userId.isBlank()) {
            boolean targeted =
                    targetRepo.existsByFeatureFlag_IdAndUserId(flag.getId(), userId);
            if (targeted) {
                return EvaluationResult.on("TARGETED_USER");
            }
        }

        int rollout = flag.getRolloutPercent();
        if (rollout <= 0) return EvaluationResult.off("ROLLOUT_0");
        if (rollout >= 100) return EvaluationResult.on("ROLLOUT_100");

        int bucket = stableBucket(featureKey, userId);
        return bucket < rollout
                ? EvaluationResult.on("ROLLOUT_BUCKET_" + bucket)
                : EvaluationResult.off("ROLLOUT_BUCKET_" + bucket);
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
