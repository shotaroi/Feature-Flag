package com.shotaroi.featureflags.dto;

import com.shotaroi.featureflags.domain.Environment;
import jakarta.validation.constraints.*;

public class AdminDtos {

    public record CreateFlagRequest(
            @NotBlank String featureKey,
            @NotNull Environment environment,
            boolean enabled,
            @Min(0) @Max(100) int rolloutPercent
    ) {}

    public record UpdateFlagRequest(
            boolean enabled,
            @Min(0) @Max(100) int rolloutPercent
    ) {}

    public record AddTargetRequest(@NotBlank String userId) {}

    public record CreateApiKeyRequest(
            @NotBlank String name,
            Environment environment
    ) {}
}
