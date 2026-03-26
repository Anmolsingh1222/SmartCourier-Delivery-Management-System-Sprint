package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import com.smartcourier.delivery.repository.DeliveryRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTransitionEventTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private TrackingEventPublisher trackingEventPublisher;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository, trackingEventPublisher);
    }

    @Test
    void markPickedUpEmitsPickedUpEvent() {
        DeliveryEntity entity = new DeliveryEntity();
        entity.setId(123L);
        entity.setTrackingNumber("SC123");
        entity.setCustomerId(50L);
        entity.setStatus(DeliveryStatus.BOOKED);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        when(deliveryRepository.findById(123L)).thenReturn(Optional.of(entity));
        when(deliveryRepository.save(any(DeliveryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = deliveryService.markPickedUp(123L, "ADMIN");

        assertEquals("PICKED_UP", response.status());
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> description = ArgumentCaptor.forClass(String.class);
        verify(trackingEventPublisher).publish(any(DeliveryEntity.class), code.capture(), description.capture());
        assertEquals("PICKED_UP", code.getValue());
    }
}
