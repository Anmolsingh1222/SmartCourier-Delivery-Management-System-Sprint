package com.smartcourier.admin.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHubRequest(@NotBlank String code, @NotBlank String name, @NotBlank String city) {
}
