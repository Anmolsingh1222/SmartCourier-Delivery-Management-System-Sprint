package com.smartcourier.tracking.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RequestContextServiceTest {

    private final RequestContextService requestContextService = new RequestContextService();

    @Test
    void allowsSystemRole() {
        assertDoesNotThrow(() -> requestContextService.assertAdminOrSystem("SYSTEM"));
    }

    @Test
    void blocksCustomerRole() {
        assertThrows(AccessDeniedException.class, () -> requestContextService.assertAdminOrSystem("CUSTOMER"));
    }
}
