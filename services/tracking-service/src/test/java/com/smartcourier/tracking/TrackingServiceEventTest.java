package com.smartcourier.tracking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartcourier.tracking.domain.TrackingEventEntity;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import com.smartcourier.tracking.service.TrackingService;
import com.smartcourier.tracking.web.dto.CreateTrackingEventRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackingServiceEventTest {

    @Mock private TrackingEventRepository trackingEventRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DeliveryProofRepository deliveryProofRepository;

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService(trackingEventRepository, documentRepository, deliveryProofRepository);
    }

    private TrackingEventEntity makeEvent(Long id, Long deliveryId, String tracking, String code) {
        TrackingEventEntity e = new TrackingEventEntity();
        e.setId(id); e.setDeliveryId(deliveryId);
        e.setTrackingNumber(tracking); e.setEventCode(code);
        e.setDescription(code + " event"); e.setEventTime(Instant.now());
        return e;
    }

    @Test
    void byTrackingReturnsEventsInOrder() {
        when(trackingEventRepository.findByTrackingNumberOrderByEventTimeDesc("SC123"))
                .thenReturn(List.of(
                        makeEvent(2L, 1L, "SC123", "IN_TRANSIT"),
                        makeEvent(1L, 1L, "SC123", "BOOKED")));

        var result = trackingService.byTracking("SC123");

        assertEquals(2, result.size());
        assertEquals("IN_TRANSIT", result.get(0).eventCode());
        assertEquals("BOOKED", result.get(1).eventCode());
    }

    @Test
    void timelineReturnsByDeliveryId() {
        when(trackingEventRepository.findByDeliveryIdOrderByEventTimeDesc(5L))
                .thenReturn(List.of(makeEvent(1L, 5L, "SC005", "DELIVERED")));

        var result = trackingService.timeline(5L);

        assertEquals(1, result.size());
        assertEquals("DELIVERED", result.get(0).eventCode());
    }

    @Test
    void latestReturnsFirstEvent() {
        when(trackingEventRepository.findByDeliveryIdOrderByEventTimeDesc(5L))
                .thenReturn(List.of(
                        makeEvent(3L, 5L, "SC005", "OUT_FOR_DELIVERY"),
                        makeEvent(2L, 5L, "SC005", "IN_TRANSIT")));

        var result = trackingService.latest(5L);

        assertEquals("OUT_FOR_DELIVERY", result.eventCode());
    }

    @Test
    void latestThrowsWhenNoEvents() {
        when(trackingEventRepository.findByDeliveryIdOrderByEventTimeDesc(99L))
                .thenReturn(List.of());

        assertThrows(Exception.class, () -> trackingService.latest(99L));
    }

    @Test
    void createEventStoresAllFields() {
        Instant now = Instant.now();
        CreateTrackingEventRequest req = new CreateTrackingEventRequest(7L, "SC007", "PICKED_UP", "Parcel picked up", now);

        when(trackingEventRepository.save(any(TrackingEventEntity.class))).thenAnswer(inv -> {
            TrackingEventEntity e = inv.getArgument(0);
            e.setId(55L);
            return e;
        });

        var result = trackingService.createEvent(req);

        assertEquals("SC007", result.trackingNumber());
        assertEquals("PICKED_UP", result.eventCode());
        assertEquals("Parcel picked up", result.description());
    }

    @Test
    void trackByTrackingNumberReturnsMap() {
        when(trackingEventRepository.findByTrackingNumberOrderByEventTimeDesc("SC999"))
                .thenReturn(List.of(makeEvent(1L, 9L, "SC999", "BOOKED")));

        var result = trackingService.track("SC999");

        assertEquals("SC999", result.get("trackingNumber"));
        assertEquals("BOOKED", result.get("latestStatus"));
        assertEquals(1, result.get("totalEvents"));
    }
}
