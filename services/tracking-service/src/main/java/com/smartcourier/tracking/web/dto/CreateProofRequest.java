package com.smartcourier.tracking.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProofRequest(@NotBlank String proofType, @NotBlank String proofUrl, @NotBlank String recipientName) {
}
