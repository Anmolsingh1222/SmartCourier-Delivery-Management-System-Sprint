package com.smartcourier.delivery.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PickupScheduleRequest(@NotBlank String pickupSlot) {
}
