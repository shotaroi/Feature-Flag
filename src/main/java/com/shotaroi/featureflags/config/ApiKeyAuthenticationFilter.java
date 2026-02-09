package com.shotaroi.featureflags.config;

import com.shotaroi.featureflags.domain.ApiKey;
import com.shotaroi.featureflags.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests to /api/flags/** using the X-API-Key header.
 * If the key is valid and enabled, sets SecurityContext and continues; otherwise returns 401.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader(API_KEY_HEADER);

        if (key == null || key.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey");
            response.getWriter().write("{\"error\":\"Missing X-API-Key header\"}");
            response.setContentType("application/json");
            return;
        }

        var apiKey = apiKeyService.validate(key);
        if (apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey");
            response.getWriter().write("{\"error\":\"Invalid or revoked API key\"}");
            response.setContentType("application/json");
            return;
        }

        ApiKey keyEntity = apiKey.get();
        var auth = new UsernamePasswordAuthenticationToken(
                keyEntity.getName(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only apply to evaluation endpoint, not admin or h2-console
        return !path.startsWith("/api/flags");
    }
}
