package com.shotaroi.featureflags.service;

import com.shotaroi.featureflags.domain.ApiKey;
import com.shotaroi.featureflags.domain.Environment;
import com.shotaroi.featureflags.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class ApiKeyService {

    private static final String KEY_PREFIX = "fk_";
    private static final int KEY_BYTES = 16;

    private final ApiKeyRepository apiKeyRepo;

    public ApiKeyService(ApiKeyRepository apiKeyRepo) {
        this.apiKeyRepo = apiKeyRepo;
    }

    /**
     * Validates the raw API key and returns the associated ApiKey if valid and enabled.
     */
    @Transactional(readOnly = true)
    public Optional<ApiKey> validate(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return Optional.empty();
        String hash = hashKey(rawKey);
        return apiKeyRepo.findByKeyHashAndEnabledTrue(hash);
    }

    /**
     * Creates a new API key. The returned raw key is only shown once and must be stored by the client.
     */
    @Transactional
    public CreateApiKeyResult create(String name, Environment environment) {
        String rawKey = generateRawKey();
        String keyHash = hashKey(rawKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setName(name);
        apiKey.setKeyHash(keyHash);
        apiKey.setEnvironment(environment);
        apiKey.setEnabled(true);
        apiKeyRepo.save(apiKey);

        return new CreateApiKeyResult(rawKey, apiKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> list() {
        return apiKeyRepo.findAll();
    }

    @Transactional
    public void revoke(Long id) {
        ApiKey apiKey = apiKeyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + id));
        apiKey.setEnabled(false);
        apiKeyRepo.save(apiKey);
    }

    private static String generateRawKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[KEY_BYTES];
        random.nextBytes(bytes);
        return KEY_PREFIX + HexFormat.of().formatHex(bytes);
    }

    static String hashKey(String rawKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record CreateApiKeyResult(String rawKey, ApiKey apiKey) {}
}
