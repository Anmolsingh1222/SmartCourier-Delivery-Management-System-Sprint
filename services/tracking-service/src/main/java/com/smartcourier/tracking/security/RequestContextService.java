package com.smartcourier.tracking.security;

import org.springframework.stereotype.Component;

@Component
public class RequestContextService {

    public void assertAdminOrSystem(String roleHeader) {
        if (roleHeader == null) {
            throw new AccessDeniedException("Admin role required");
        }
        String role = roleHeader.trim().toUpperCase();
        if (!"ADMIN".equals(role) && !"SYSTEM".equals(role)) {
            throw new AccessDeniedException("Admin role required");
        }
    }
}
