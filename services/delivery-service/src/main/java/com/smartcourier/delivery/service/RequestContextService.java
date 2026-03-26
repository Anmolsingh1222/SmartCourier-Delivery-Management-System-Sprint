package com.smartcourier.delivery.service;

import org.springframework.stereotype.Component;

@Component
public class RequestContextService {

    public Long userId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new AccessDeniedException("Missing X-User-Id header");
        }
        return Long.valueOf(userIdHeader);
    }

    public String role(String roleHeader) {
        return roleHeader == null ? "CUSTOMER" : roleHeader;
    }
}
