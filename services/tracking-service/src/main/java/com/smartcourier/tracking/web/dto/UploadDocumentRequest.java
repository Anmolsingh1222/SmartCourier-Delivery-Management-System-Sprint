package com.smartcourier.tracking.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadDocumentRequest(
        @NotNull Long deliveryId,
        @NotBlank String trackingNumber,
        @NotBlank String fileName,
        @NotBlank String fileType,
        @NotBlank String fileUrl) {
}
