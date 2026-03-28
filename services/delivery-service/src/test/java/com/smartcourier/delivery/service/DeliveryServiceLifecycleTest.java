package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import com.smartcourier.delivery.repository.DeliveryRepository;
import com.smartcourier.delivery.web.dto.PickupScheduleRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceLifecycleTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private TrackingEventPublisher trackingEventPublisher;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository, trackingEventPublisher);
    }

    private DeliveryEntity bookedEntity(Long id, Long customerId) {
        DeliveryEntity e = new DeliveryEntity();
        e.setId(id); e.setCustomerId(customerId);
        e.setTrackingNumber("SC" + id);
        e.setStatus(DeliveryStatus.BOOKED);
        e.setSenderName("S"); e.setReceiverName("R");
        e.setReceiverPhone("9999999999");
        e.setPickupAddress("A"); e.setDestinationAddress("B");
        e.setPackageWeightKg(new BigDecimal("1.0"));
        e.setPackageType("BOX"); e.setServiceType("STANDARD");
        e.setQuotedPrice(new BigDecimal("138.00"));
        e.setCreatedAt(Instant.now()); e.setUpdatedAt(Instant.now());
        return e;
    }

    @Test
    void bookTransitionsDraftToBooked() {
        DeliveryEntity entity = bookedEntity(1L, 10L);
        entity.setStatus(DeliveryStatus.DRAFT);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = deliveryService.book(1L, 10L, "CUSTOMER");

        assertEquals("BOOKED", result.status());
        verify(trackingEventPublisher).publish(any(), eq("BOOKED"), any());
    }

    @Test
    void bookRejectsNonDraftDelivery() {
        DeliveryEntity entity = bookedEntity(2L, 10L);
        entity.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findById(2L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class, () -> deliveryService.book(2L, 10L, "CUSTOMER"));
    }

    @Test
    void schedulePickupRequiresBookedStatus() {
        DeliveryEntity entity = bookedEntity(3L, 10L);
        entity.setStatus(DeliveryStatus.DRAFT);
        when(deliveryRepository.findById(3L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class,
                () -> deliveryService.schedulePickup(3L, 10L, "CUSTOMER", new PickupScheduleRequest("2026-04-01 10:00")));
    }

    @Test
    void schedulePickupSetsSlot() {
        DeliveryEntity entity = bookedEntity(4L, 10L);
        when(deliveryRepository.findById(4L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = deliveryService.schedulePickup(4L, 10L, "CUSTOMER", new PickupScheduleRequest("2026-04-01 10:00"));

        assertEquals("2026-04-01 10:00", result.pickupSlot());
    }

    @Test
    void cancelDeliveredParcelThrows() {
        DeliveryEntity entity = bookedEntity(5L, 10L);
        entity.setStatus(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class, () -> deliveryService.cancel(5L, 10L, "CUSTOMER"));
    }

    @Test
    void cancelSetsStatusCancelled() {
        DeliveryEntity entity = bookedEntity(6L, 10L);
        when(deliveryRepository.findById(6L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = deliveryService.cancel(6L, 10L, "CUSTOMER");

        assertEquals("CANCELLED", result.status());
        verify(trackingEventPublisher).publish(any(), eq("CANCELLED"), any());
    }

    @Test
    void nonOwnerCannotCancelDelivery() {
        DeliveryEntity entity = bookedEntity(7L, 10L);
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(entity));

        assertThrows(AccessDeniedException.class, () -> deliveryService.cancel(7L, 99L, "CUSTOMER"));
    }

    @Test
    void adminCanCancelAnyDelivery() {
        DeliveryEntity entity = bookedEntity(8L, 10L);
        when(deliveryRepository.findById(8L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = deliveryService.cancel(8L, 999L, "ADMIN");

        assertEquals("CANCELLED", result.status());
    }

    @Test
    void fullTransitionChainWorks() {
        DeliveryEntity entity = bookedEntity(9L, 10L);
        when(deliveryRepository.findById(9L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var r1 = deliveryService.markPickedUp(9L, "ADMIN");
        assertEquals("PICKED_UP", r1.status());

        entity.setStatus(DeliveryStatus.PICKED_UP);
        var r2 = deliveryService.markInTransit(9L, "ADMIN");
        assertEquals("IN_TRANSIT", r2.status());

        entity.setStatus(DeliveryStatus.IN_TRANSIT);
        var r3 = deliveryService.markOutForDelivery(9L, "ADMIN");
        assertEquals("OUT_FOR_DELIVERY", r3.status());

        entity.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
        var r4 = deliveryService.markDelivered(9L, "ADMIN");
        assertEquals("DELIVERED", r4.status());
    }

    @Test
    void nonAdminCannotMarkPickedUp() {
        DeliveryEntity entity = bookedEntity(10L, 10L);
        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThrows(AccessDeniedException.class, () -> deliveryService.markPickedUp(10L, "CUSTOMER"));
    }
}
