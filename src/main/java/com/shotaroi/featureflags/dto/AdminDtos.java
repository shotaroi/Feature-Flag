package com.shotaroi.featureflags.dto;

import jakarta.validation.constraints.*;

public class AdminDtos {

    public record CreateFlagRequest(
            @NotBlank String featureKey,
            boolean enabled,
            @Min(0) @Max(100) int rolloutPercent
    ) {}

    public record UpdateFlagRequest(
            boolean enabled,
            @Min(0) @Max(100) int rolloutPercent
    ) {}

    public record AddTargetRequest(@NotBlank String userId) {}
}
