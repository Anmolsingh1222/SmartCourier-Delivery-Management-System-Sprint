package com.smartcourier.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import com.smartcourier.admin.repository.AdminUserRepository;
import com.smartcourier.admin.repository.DeliveryExceptionRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.web.dto.ResolveExceptionRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private HubRepository hubRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private DeliveryExceptionRepository deliveryExceptionRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(hubRepository, adminUserRepository, deliveryExceptionRepository);
    }

    @Test
    void resolveMarksExceptionResolved() {
        DeliveryExceptionEntity entity = new DeliveryExceptionEntity();
        entity.setId(5L);
        entity.setResolved(false);
        when(deliveryExceptionRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(deliveryExceptionRepository.save(entity)).thenReturn(entity);

        var updated = adminService.resolve(5L, new ResolveExceptionRequest("Resolved manually"));

        assertEquals(true, updated.isResolved());
        assertEquals("Resolved manually", updated.getResolution());
    }
}
