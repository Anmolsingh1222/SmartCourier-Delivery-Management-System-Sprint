package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import com.smartcourier.delivery.repository.DeliveryRepository;
import com.smartcourier.delivery.web.dto.CreateDeliveryRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

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
    void createStartsInDraftAndPublishesEvent() {
        CreateDeliveryRequest request = new CreateDeliveryRequest(
                "Sender", "Receiver", "9999999999", "A", "B", new BigDecimal("2.0"), "BOX", "STANDARD");

        when(deliveryRepository.save(any(DeliveryEntity.class))).thenAnswer(inv -> {
            DeliveryEntity entity = inv.getArgument(0);
            entity.setId(7L);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());
            return entity;
        });

        var response = deliveryService.create(101L, request);

        assertEquals("DRAFT", response.status());
        verify(trackingEventPublisher).publish(any(DeliveryEntity.class), any(String.class), any(String.class));
    }

    @Test
    void adminTransitionRejectsInvalidFromState() {
        DeliveryEntity entity = new DeliveryEntity();
        entity.setId(10L);
        entity.setStatus(DeliveryStatus.DRAFT);
        entity.setCustomerId(101L);
        when(deliveryRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class, () -> deliveryService.markInTransit(10L, "ADMIN"));
    }
}
