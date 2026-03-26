package com.smartcourier.tracking.web.dto;

import java.time.Instant;

public record TrackingEventResponse(
        Long id,
        Long deliveryId,
        String trackingNumber,
        String eventCode,
        String description,
        Instant eventTime,
        Instant createdAt) {
}
