package com.smartcourier.tracking.messaging;

import java.time.Instant;

public record DeliveryEventMessage(
        Long deliveryId,
        String trackingNumber,
        Long customerId,
        String eventCode,
        String description,
        String status,
        String serviceType,
        String receiverName,
        String destinationAddress,
        Instant eventTime) {
}
