package com.smartcourier.delivery.domain;

public enum DeliveryStatus {
    DRAFT,
    BOOKED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELAYED,
    FAILED,
    RETURNED,
    CANCELLED
}
