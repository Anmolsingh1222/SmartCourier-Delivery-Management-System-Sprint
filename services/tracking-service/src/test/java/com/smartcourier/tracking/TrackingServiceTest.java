package com.smartcourier.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.smartcourier.tracking.domain.TrackingEventEntity;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import com.smartcourier.tracking.service.TrackingService;
import com.smartcourier.tracking.web.dto.CreateTrackingEventRequest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DeliveryProofRepository deliveryProofRepository;

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService(trackingEventRepository, documentRepository, deliveryProofRepository);
    }

    @Test
    void createEventPersistsTrackingEvent() {
        CreateTrackingEventRequest request = new CreateTrackingEventRequest(
                11L, "SC123", "BOOKED", "Booked", Instant.now());

        when(trackingEventRepository.save(any(TrackingEventEntity.class))).thenAnswer(inv -> {
            TrackingEventEntity entity = inv.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        var response = trackingService.createEvent(request);

        assertEquals("SC123", response.trackingNumber());
        assertEquals("BOOKED", response.eventCode());
    }
}
