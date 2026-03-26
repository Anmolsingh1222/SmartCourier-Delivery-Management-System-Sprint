package com.smartcourier.admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateExceptionRequest(@NotNull Long deliveryId, @NotBlank String status, @NotBlank String reason) {
}
