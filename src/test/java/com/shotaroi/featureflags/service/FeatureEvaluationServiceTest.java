package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeatureEvaluationServiceTest {

    private FeatureFlagRepository flagRepo;
    private FeatureTargetRepository targetRepo;
    private FeatureEvaluationService service;

    @BeforeEach
    void setUp() {
        flagRepo = mock(FeatureFlagRepository.class);
        targetRepo = mock(FeatureTargetRepository.class);
        service = new FeatureEvaluationService(flagRepo, targetRepo, new SimpleMeterRegistry());
    }

    @Test
    void flagNotFound_returnsOff() {
        when(flagRepo.findByFeatureKeyAndEnvironment("x", Environment.PROD)).thenReturn(Optional.empty());

        var res = service.evaluate("x", Environment.PROD, "alice");

        assertFalse(res.enabled());
        assertEquals("FLAG_NOT_FOUND", res.reason());
    }

    @Test
    void disabledFlag_returnsOff_evenIfTargeted() {
        FeatureFlag flag = flag("new_dashboard", Environment.DEV, false, 100, 1L);
        when(flagRepo.findByFeatureKeyAndEnvironment("new_dashboard", Environment.DEV)).thenReturn(Optional.of(flag));
        when(targetRepo.existsByFeatureFlag_IdAndUserId(1L, "alice")).thenReturn(true);

        var res = service.evaluate("new_dashboard", Environment.DEV, "alice");

        assertFalse(res.enabled());
        assertEquals("FLAG_DISABLED", res.reason());
    }

    @Test
    void targetedUser_returnsOn_whenEnabled() {
        FeatureFlag flag = flag("new_dashboard", Environment.STAGING, true, 0, 1L);
        when(flagRepo.findByFeatureKeyAndEnvironment("new_dashboard", Environment.STAGING)).thenReturn(Optional.of(flag));
        when(targetRepo.existsByFeatureFlag_IdAndUserId(1L, "alice")).thenReturn(true);

        var res = service.evaluate("new_dashboard", Environment.STAGING, "alice");

        assertTrue(res.enabled());
        assertEquals("TARGETED_USER", res.reason());
    }

    @Test
    void rollout0_returnsOff_whenNotTargeted() {
        FeatureFlag flag = flag("new_dashboard", Environment.PROD, true, 0, 1L);
        when(flagRepo.findByFeatureKeyAndEnvironment("new_dashboard", Environment.PROD)).thenReturn(Optional.of(flag));
        when(targetRepo.existsByFeatureFlag_IdAndUserId(1L, "alice")).thenReturn(false);

        var res = service.evaluate("new_dashboard", Environment.PROD, "alice");

        assertFalse(res.enabled());
        assertEquals("ROLLOUT_0", res.reason());
    }

    @Test
    void rollout100_returnsOn_whenNotTargeted() {
        FeatureFlag flag = flag("new_dashboard", Environment.PROD, true, 100, 1L);
        when(flagRepo.findByFeatureKeyAndEnvironment("new_dashboard", Environment.PROD)).thenReturn(Optional.of(flag));
        when(targetRepo.existsByFeatureFlag_IdAndUserId(1L, "alice")).thenReturn(false);

        var res = service.evaluate("new_dashboard", Environment.PROD, "alice");

        assertTrue(res.enabled());
        assertEquals("ROLLOUT_100", res.reason());
    }

    @Test
    void deterministicBucket_sameUserSameResult() {
        FeatureFlag flag = flag("new_dashboard", Environment.PROD, true, 30, 1L);
        when(flagRepo.findByFeatureKeyAndEnvironment("new_dashboard", Environment.PROD)).thenReturn(Optional.of(flag));
        when(targetRepo.existsByFeatureFlag_IdAndUserId(1L, "alice")).thenReturn(false);

        var r1 = service.evaluate("new_dashboard", Environment.PROD, "alice");
        var r2 = service.evaluate("new_dashboard", Environment.PROD, "alice");

        assertEquals(r1.enabled(), r2.enabled());
        assertEquals(r1.reason(), r2.reason());
    }

    private FeatureFlag flag(String key, Environment env, boolean enabled, int rollout, long id) {
        FeatureFlag f = new FeatureFlag();
        f.setFeatureKey(key);
        f.setEnvironment(env);
        f.setEnabled(enabled);
        f.setRolloutPercent(rollout);

        // If your FeatureFlag has no setId(), do this instead:
        // - add a package-private setter, OR
        // - in the service use flag.getId() only after save, OR
        // - in tests, mock the ID with reflection
        //
        // If you DO have setId:
        try {
            var m = FeatureFlag.class.getMethod("setId", Long.class);
            m.invoke(f, id);
        } catch (Exception ignored) {}

        return f;
    }
}
