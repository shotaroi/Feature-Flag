package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.ApiKey;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/api-keys")
@Tag(name = "Admin – API keys", description = "Create, list, and revoke API keys for the evaluation endpoint. Requires HTTP Basic (admin).")
public class ApiKeyAdminController {

    private final ApiKeyService apiKeyService;

    public ApiKeyAdminController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(summary = "Create API key", description = "Returns rawKey once; store it securely. It cannot be retrieved later.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody AdminDtos.CreateApiKeyRequest req) {
        ApiKeyService.CreateApiKeyResult result = apiKeyService.create(req.name(), req.environment());
        return Map.of(
                "rawKey", result.rawKey(),
                "apiKey", result.apiKey()
        );
    }

    @Operation(summary = "List all API keys (key value is never returned)")
    @GetMapping
    public List<ApiKey> list() {
        return apiKeyService.list();
    }

    @Operation(summary = "Revoke an API key (sets enabled=false)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long id) {
        apiKeyService.revoke(id);
    }
}
