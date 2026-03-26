package com.smartcourier.auth.service;

import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public Long userId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getDetails() instanceof Map<?, ?> details)) {
            throw new BadCredentialsException("Missing authentication");
        }
        Object sub = details.get("sub");
        if (sub == null) {
            throw new BadCredentialsException("Missing user id");
        }
        return Long.valueOf(String.valueOf(sub));
    }
}
