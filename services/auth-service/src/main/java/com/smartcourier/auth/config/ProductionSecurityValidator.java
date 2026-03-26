package com.smartcourier.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionSecurityValidator {

    private static final String DEFAULT_SECRET = "smartcourier-super-secret-key-change-in-prod-32-plus-bytes";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@12345";

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${bootstrap.admin.enabled:true}")
    private boolean bootstrapAdminEnabled;

    @Value("${bootstrap.admin.password:}")
    private String bootstrapAdminPassword;

    @PostConstruct
    void validate() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters in prod profile");
        }
        if (DEFAULT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("Default JWT secret is not allowed in prod profile");
        }
        if (bootstrapAdminEnabled && DEFAULT_ADMIN_PASSWORD.equals(bootstrapAdminPassword)) {
            throw new IllegalStateException("Default bootstrap admin password is not allowed in prod profile");
        }
    }
}
