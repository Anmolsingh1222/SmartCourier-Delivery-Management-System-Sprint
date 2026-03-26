package com.smartcourier.delivery.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateDeliveryRequest(
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String destinationAddress,
        @NotNull @DecimalMin("0.01") BigDecimal packageWeightKg,
        @NotBlank String packageType,
        @NotBlank String serviceType) {
}
