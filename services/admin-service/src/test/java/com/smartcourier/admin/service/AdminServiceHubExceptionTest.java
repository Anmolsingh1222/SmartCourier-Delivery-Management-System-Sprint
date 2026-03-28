package com.smartcourier.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartcourier.admin.domain.DeliveryExceptionEntity;
import com.smartcourier.admin.domain.HubEntity;
import com.smartcourier.admin.repository.AdminUserRepository;
import com.smartcourier.admin.repository.DeliveryExceptionRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.web.dto.CreateExceptionRequest;
import com.smartcourier.admin.web.dto.CreateHubRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceHubExceptionTest {

    @Mock private HubRepository hubRepository;
    @Mock private AdminUserRepository adminUserRepository;
    @Mock private DeliveryExceptionRepository deliveryExceptionRepository;
    @Mock private DeliveryQueryPort deliveryQueryPort;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(hubRepository, adminUserRepository, deliveryExceptionRepository, deliveryQueryPort);
    }

    @Test
    void createHubPersistsWithUpperCaseCode() {
        when(hubRepository.save(any(HubEntity.class))).thenAnswer(inv -> {
            HubEntity h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        var hub = adminService.createHub(new CreateHubRequest("del", "Delhi Hub", "Delhi"));

        assertEquals("DEL", hub.getCode());
        assertEquals("Delhi Hub", hub.getName());
    }

    @Test
    void listHubsReturnsAll() {
        HubEntity h1 = new HubEntity(); h1.setCode("DEL"); h1.setName("Delhi");
        HubEntity h2 = new HubEntity(); h2.setCode("MUM"); h2.setName("Mumbai");
        when(hubRepository.findAll()).thenReturn(List.of(h1, h2));

        var hubs = adminService.hubs();

        assertEquals(2, hubs.size());
    }

    @Test
    void createExceptionPersistsWithUpperCaseStatus() {
        when(deliveryExceptionRepository.save(any(DeliveryExceptionEntity.class))).thenAnswer(inv -> {
            DeliveryExceptionEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        var exception = adminService.createException(new CreateExceptionRequest(5L, "lost", "Parcel lost in transit"));

        assertEquals("LOST", exception.getStatus());
        assertEquals("Parcel lost in transit", exception.getReason());
        assertEquals(5L, exception.getDeliveryId());
    }

    @Test
    void resolveNonExistentExceptionThrows() {
        when(deliveryExceptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.resolve(999L, new com.smartcourier.admin.web.dto.ResolveExceptionRequest("test")));
    }

    @Test
    void reportsReturnsCorrectCounts() {
        when(deliveryExceptionRepository.count()).thenReturn(10L);
        when(deliveryExceptionRepository.findAll()).thenReturn(List.of());
        when(deliveryExceptionRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of());

        var reports = adminService.reports();

        assertEquals(10L, reports.get("totalExceptions"));
        assertEquals(0L, reports.get("resolved"));
        assertEquals(0, reports.get("open"));
    }

    @Test
    void deliveriesReturnsExceptionList() {
        DeliveryExceptionEntity e = new DeliveryExceptionEntity();
        e.setId(1L); e.setDeliveryId(5L); e.setStatus("LOST");
        when(deliveryExceptionRepository.findAll()).thenReturn(List.of(e));

        var result = adminService.deliveries();

        assertEquals(1, result.size());
        assertEquals("LOST", result.get(0).getStatus());
    }
}
