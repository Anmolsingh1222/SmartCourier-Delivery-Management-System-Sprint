package com.smartcourier.admin.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RequestContextServiceTest {

    private final RequestContextService requestContextService = new RequestContextService();

    @Test
    void allowsAdminRole() {
        assertDoesNotThrow(() -> requestContextService.assertAdmin("ADMIN"));
    }

    @Test
    void blocksMissingRole() {
        assertThrows(AccessDeniedException.class, () -> requestContextService.assertAdmin(null));
    }
}
