package com.shotaroi.featureflags.config;

import com.shotaroi.featureflags.service.ApiKeyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyService apiKeyService) throws Exception {
        var apiKeyFilter = new ApiKeyAuthenticationFilter(apiKeyService);

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/flags/**").authenticated()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Health: public for load balancers / k8s probes
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Metrics and Prometheus: admin only (or use same as health for simple setups)
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(h -> h.frameOptions(frame -> frame.disable()))

                .build();
    }

//    @Bean
//    PasswordEncoder passwordEncoder() {
//        // BCrypt is the standard safe hashing algorithm for passwords
//        return new BCryptPasswordEncoder();
//    }
}
