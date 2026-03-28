package com.smartcourier.delivery.service;

import com.smartcourier.delivery.config.RabbitMQConfig;
import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.messaging.DeliveryEventMessage;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Component
public class RabbitMQEventPublisher implements TrackingEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RestClient restClient;

    public RabbitMQEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${clients.tracking.base-url}") String trackingBaseUrl) {
        this.rabbitTemplate = rabbitTemplate;
        this.restClient = RestClient.builder().baseUrl(trackingBaseUrl).build();
    }

    @Override
    public void publish(DeliveryEntity delivery, String eventCode, String description) {
        // 1. Publish to RabbitMQ for async notification processing
        try {
            DeliveryEventMessage message = new DeliveryEventMessage(
                    delivery.getId(),
                    delivery.getTrackingNumber(),
                    delivery.getCustomerId(),
                    eventCode,
                    description,
                    delivery.getStatus().name(),
                    delivery.getServiceType(),
                    delivery.getReceiverName(),
                    delivery.getDestinationAddress(),
                    Instant.now());

            String routingKey = "delivery.status." + eventCode.toLowerCase();
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, message);
            log.info("Published delivery event to RabbitMQ: delivery={} event={}", delivery.getId(), eventCode);
        } catch (Exception ex) {
            log.warn("Failed to publish to RabbitMQ: delivery={} event={}", delivery.getId(), eventCode, ex);
        }

        // 2. Also call tracking service directly for real-time tracking events
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
