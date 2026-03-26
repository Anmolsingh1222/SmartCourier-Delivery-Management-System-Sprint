package com.smartcourier.delivery.service;

import com.smartcourier.delivery.domain.DeliveryEntity;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TrackingEventPublisher implements TrackingEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(TrackingEventPublisher.class);
    private final RestClient restClient;

    public TrackingEventPublisher(@Value("${clients.tracking.base-url}") String trackingBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(trackingBaseUrl).build();
    }

    @Override
    public void publish(DeliveryEntity delivery, String eventCode, String description) {
        try {
            Map<String, Object> payload = Map.of(
                    "deliveryId", delivery.getId(),
                    "trackingNumber", delivery.getTrackingNumber(),
                    "eventCode", eventCode,
                    "description", description,
                    "eventTime", Instant.now().toString());

            restClient.post()
                    .uri("/api/tracking/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Role", "SYSTEM")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to publish tracking event={} for delivery={}", eventCode, delivery.getId(), ex);
        }
    }
}
