package com.smartcourier.admin.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveExceptionRequest(@NotBlank String resolution) {
}
