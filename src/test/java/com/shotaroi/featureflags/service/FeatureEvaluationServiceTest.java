package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.FeatureFlag;
import com.shotaroi.featureflags.repository.FeatureFlagRepository;
import com.shotaroi.featureflags.repository.FeatureTargetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class FeatureEvaluationServiceTest {

    private FeatureFlagRepository flagRepo;
    private FeatureTargetRepository targetRepo;
    private FeatureEvaluationService service;

    @BeforeEach
    void setUp() {
        flagRepo = mock(FeatureFlagRepository.class);
        targetRepo = mock(FeatureTargetRepository.class);
        service = new FeatureEvaluationService(flagRepo, targetRepo);
    }

    
}
