package com.smartcourier.admin.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminDeliveryView(
        Long id,
        String trackingNumber,
        Long customerId,
        String status,
        String senderName,
        String receiverName,
        String receiverPhone,
        String pickupAddress,
        String destinationAddress,
        BigDecimal packageWeightKg,
        String packageType,
        String serviceType,
        BigDecimal quotedPrice,
        String pickupSlot,
        Instant createdAt,
        Instant updatedAt) {}
