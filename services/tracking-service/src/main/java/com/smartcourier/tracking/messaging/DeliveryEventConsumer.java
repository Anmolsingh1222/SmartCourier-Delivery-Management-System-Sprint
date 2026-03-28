package com.smartcourier.tracking.messaging;

import com.smartcourier.tracking.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_QUEUE)
    public void handleDeliveryEvent(DeliveryEventMessage message) {
        log.info("📦 Delivery event received: tracking={} event={} status={} customer={}",
                message.trackingNumber(),
                message.eventCode(),
                message.status(),
                message.customerId());

        // Log notification based on event type
        switch (message.eventCode()) {
            case "CREATED" ->
                log.info("🆕 New delivery created: {} → {} ({})",
                        message.trackingNumber(), message.destinationAddress(), message.serviceType());
            case "BOOKED" ->
                log.info("✅ Delivery booked: {} for {} → {}",
                        message.trackingNumber(), message.receiverName(), message.destinationAddress());
            case "PICKED_UP" ->
                log.info("🚚 Parcel picked up: {} — in transit to {}",
                        message.trackingNumber(), message.destinationAddress());
            case "IN_TRANSIT" ->
                log.info("🔄 Parcel in transit: {} heading to {}",
                        message.trackingNumber(), message.destinationAddress());
            case "OUT_FOR_DELIVERY" ->
                log.info("🏃 Out for delivery: {} — delivering to {}",
                        message.trackingNumber(), message.receiverName());
            case "DELIVERED" ->
                log.info("🎉 Delivered successfully: {} to {} at {}",
                        message.trackingNumber(), message.receiverName(), message.destinationAddress());
            case "CANCELLED" ->
                log.info("❌ Delivery cancelled: {}", message.trackingNumber());
            default ->
                log.info("📋 Event {}: {}", message.eventCode(), message.description());
        }
    }
}
