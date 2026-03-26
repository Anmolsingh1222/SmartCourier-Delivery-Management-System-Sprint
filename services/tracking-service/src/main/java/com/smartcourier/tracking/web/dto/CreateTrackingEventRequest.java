package com.smartcourier.tracking.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateTrackingEventRequest(
        @NotNull Long deliveryId,
        @NotBlank String trackingNumber,
        @NotBlank String eventCode,
        @NotBlank String description,
        @NotNull Instant eventTime) {
}
