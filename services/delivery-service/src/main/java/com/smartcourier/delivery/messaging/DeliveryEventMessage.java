package com.smartcourier.delivery.messaging;

import java.io.Serializable;
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
        Instant eventTime) implements Serializable {
}
