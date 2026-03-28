package com.smartcourier.admin.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestContextServiceTest {

    private final RequestContextService service = new RequestContextService();

    @Test
    void assertAdminPassesForAdminRole() {
        assertDoesNotThrow(() -> service.assertAdmin("ADMIN"));
    }

    @Test
    void assertAdminPassesCaseInsensitive() {
        assertDoesNotThrow(() -> service.assertAdmin("admin"));
        assertDoesNotThrow(() -> service.assertAdmin("Admin"));
    }

    @Test
    void assertAdminThrowsForCustomerRole() {
        assertThrows(AccessDeniedException.class, () -> service.assertAdmin("CUSTOMER"));
    }

    @Test
    void assertAdminThrowsForNullRole() {
        assertThrows(AccessDeniedException.class, () -> service.assertAdmin(null));
    }

    @Test
    void assertAdminThrowsForEmptyRole() {
        assertThrows(AccessDeniedException.class, () -> service.assertAdmin(""));
    }
}
