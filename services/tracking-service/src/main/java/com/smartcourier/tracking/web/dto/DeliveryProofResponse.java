package com.smartcourier.tracking.web.dto;

import java.time.Instant;

public record DeliveryProofResponse(
        Long id,
        Long deliveryId,
        String proofType,
        String proofUrl,
        String recipientName,
        Instant confirmedAt) {
}
