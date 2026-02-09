package com.shotaroi.featureflags;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(properties = {
        // Use H2 in-memory for tests (independent of your normal app.yml)
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",

        // Make tests independent of your real BasicAuth user config.
        // (We use @WithMockUser for admin/user tests.)
})
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void publicFlagsEndpoint_isAccessibleWithoutAuth() throws Exception {
        mvc.perform(get("/api/flags/some_feature/evaluate")
                        .param("environment", "PROD")
                        .param("userId", "alice"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_requireAuthentication() throws Exception {
        mvc.perform(get("/api/admin/flags"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void adminEndpoints_forbidNonAdmin() throws Exception {
        mvc.perform(get("/api/admin/flags")
                .with(user("user").roles("USER")))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void adminEndpoints_allowAdmin() throws Exception {
        mvc.perform(get("/api/admin/flags")
                        .param("environment", "DEV")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()); // 200
    }

    @Test
    void adminCanCreateFeatureFlag() throws Exception {
        mvc.perform(post("/api/admin/flags")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "featureKey": "new_dashboard",
                                  "environment": "DEV",
                                  "enabled": true,
                                  "rolloutPercent": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.featureKey").value("new_dashboard"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.rolloutPercent").value(20));
    }
}
