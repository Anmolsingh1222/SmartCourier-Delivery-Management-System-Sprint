package com.smartcourier.delivery.service;

import com.smartcourier.delivery.domain.DeliveryEntity;

public interface TrackingEventPublisherPort {

    void publish(DeliveryEntity delivery, String eventCode, String description);
}
