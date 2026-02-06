package com.shotaroi.featureflags.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // For a pure REST API, we usually disable CSRF.
                // CSRF mainly matters for browser-based logins with cookies.
                .csrf(csrf -> csrf.disable())

                // Make it stateless: server does NOT store login sessions.
                // Every request must include credentials (Basic auth header).
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules: who can access what
                .authorizeHttpRequests(auth -> auth
                        // Public evaluate endpoint
                        .requestMatchers("/api/flags/**").permitAll()

                        // H2 console (optional dev convenience)
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Admin endpoints require role ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")


                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Enable HTTP Basic auth (username/password)
                .httpBasic(Customizer.withDefaults())

                // H2 console uses frames; this allows it in dev
                .headers(h -> h.frameOptions(frame -> frame.disable()))

                .build();
    }

//    @Bean
//    PasswordEncoder passwordEncoder() {
//        // BCrypt is the standard safe hashing algorithm for passwords
//        return new BCryptPasswordEncoder();
//    }
}
