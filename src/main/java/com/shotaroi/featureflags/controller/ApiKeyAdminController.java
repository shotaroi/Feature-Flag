package com.shotaroi.featureflags.controller;

import com.shotaroi.featureflags.domain.ApiKey;
import com.shotaroi.featureflags.dto.AdminDtos;
import com.shotaroi.featureflags.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/api-keys")
public class ApiKeyAdminController {

    private final ApiKeyService apiKeyService;

    public ApiKeyAdminController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody AdminDtos.CreateApiKeyRequest req) {
        ApiKeyService.CreateApiKeyResult result = apiKeyService.create(req.name(), req.environment());
        return Map.of(
                "rawKey", result.rawKey(),
                "apiKey", result.apiKey()
        );
    }

    @GetMapping
    public List<ApiKey> list() {
        return apiKeyService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long id) {
        apiKeyService.revoke(id);
    }
}
