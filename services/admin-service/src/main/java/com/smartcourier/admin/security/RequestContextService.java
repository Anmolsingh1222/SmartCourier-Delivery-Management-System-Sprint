package com.smartcourier.admin.security;

import org.springframework.stereotype.Component;

@Component
public class RequestContextService {

    public void assertAdmin(String roleHeader) {
        if (roleHeader == null || !roleHeader.equalsIgnoreCase("ADMIN")) {
            throw new AccessDeniedException("Admin role required");
        }
    }
}
