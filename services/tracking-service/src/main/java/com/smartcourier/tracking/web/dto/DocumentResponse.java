package com.smartcourier.tracking.web.dto;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        Long deliveryId,
        String trackingNumber,
        String fileName,
        String fileType,
        String fileUrl,
        Instant uploadedAt) {
}
