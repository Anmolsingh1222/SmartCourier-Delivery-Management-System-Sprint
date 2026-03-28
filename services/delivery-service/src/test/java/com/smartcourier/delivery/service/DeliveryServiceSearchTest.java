package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import com.smartcourier.delivery.repository.DeliveryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceSearchTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private TrackingEventPublisher trackingEventPublisher;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository, trackingEventPublisher);
    }

    private DeliveryEntity makeEntity(Long id, Long customerId, String tracking, String receiver) {
        DeliveryEntity e = new DeliveryEntity();
        e.setId(id);
        e.setCustomerId(customerId);
        e.setTrackingNumber(tracking);
        e.setReceiverName(receiver);
        e.setStatus(DeliveryStatus.BOOKED);
        e.setSenderName("Sender");
        e.setReceiverPhone("9999999999");
        e.setPickupAddress("Pickup");
        e.setDestinationAddress("Delhi");
        e.setPackageWeightKg(new BigDecimal("1.0"));
        e.setPackageType("BOX");
        e.setServiceType("STANDARD");
        e.setQuotedPrice(new BigDecimal("138.00"));
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    @Test
    void adminSearchReturnsAllDeliveries() {
        when(deliveryRepository.findAllByOrderByUpdatedAtDesc())
                .thenReturn(List.of(makeEntity(1L, 10L, "SC001", "Alice"), makeEntity(2L, 20L, "SC002", "Bob")));

        var result = deliveryService.search(null, null, "ADMIN", 0L);

        assertEquals(2, result.size());
    }

    @Test
    void adminSearchFiltersByStatus() {
        when(deliveryRepository.findByStatusOrderByUpdatedAtDesc(DeliveryStatus.BOOKED))
                .thenReturn(List.of(makeEntity(1L, 10L, "SC001", "Alice")));

        var result = deliveryService.search(null, "BOOKED", "ADMIN", 0L);

        assertEquals(1, result.size());
        assertEquals("BOOKED", result.get(0).status());
    }

    @Test
    void customerSearchFiltersOwnDeliveries() {
        when(deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(makeEntity(1L, 10L, "SC001", "Alice")));

        var result = deliveryService.search(null, null, "CUSTOMER", 10L);

        assertEquals(1, result.size());
        assertEquals("SC001", result.get(0).trackingNumber());
    }

    @Test
    void customerSearchFiltersByQuery() {
        when(deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(
                        makeEntity(1L, 10L, "SC001", "Alice"),
                        makeEntity(2L, 10L, "SC002", "Bob")));

        var result = deliveryService.search("alice", null, "CUSTOMER", 10L);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).receiverName());
    }

    @Test
    void estimateReturnsCorrectPriceForExpress() {
        var result = deliveryService.estimate(
                new com.smartcourier.delivery.web.dto.PriceEstimateRequest(new BigDecimal("2.0"), "EXPRESS"));

        // base 180 + 2 * 18 = 216
        assertEquals(new BigDecimal("216.00"), result.get("estimatedPrice"));
        assertEquals("INR", result.get("currency"));
    }

    @Test
    void estimateReturnsCorrectPriceForStandard() {
        var result = deliveryService.estimate(
                new com.smartcourier.delivery.web.dto.PriceEstimateRequest(new BigDecimal("1.0"), "STANDARD"));

        // base 120 + 1 * 18 = 138
        assertEquals(new BigDecimal("138.00"), result.get("estimatedPrice"));
    }

    @Test
    void estimateReturnsCorrectPriceForInternational() {
        var result = deliveryService.estimate(
                new com.smartcourier.delivery.web.dto.PriceEstimateRequest(new BigDecimal("3.0"), "INTERNATIONAL"));

        // base 450 + 3 * 18 = 504
        assertEquals(new BigDecimal("504.00"), result.get("estimatedPrice"));
    }
}
