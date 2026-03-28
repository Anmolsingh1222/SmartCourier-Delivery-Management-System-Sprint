package com.smartcourier.tracking.messaging;

import com.smartcourier.tracking.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String fromEmail;

    public DeliveryEventConsumer(
            JavaMailSender mailSender,
            @Value("${notifications.email.enabled:false}") boolean emailEnabled,
            @Value("${notifications.email.from:noreply@smartcourier.local}") String fromEmail) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.fromEmail = fromEmail;
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_QUEUE)
    public void handleDeliveryEvent(DeliveryEventMessage message) {
        log.info("📦 Event: tracking={} event={} status={} customer={}",
                message.trackingNumber(), message.eventCode(),
                message.status(), message.customerId());

        String subject = buildSubject(message);
        String body    = buildBody(message);

        log.info("📧 Notification → [{}] {}", message.eventCode(), subject);

        if (emailEnabled) {
            sendEmail(message, subject, body);
        }
    }

    private void sendEmail(DeliveryEventMessage message, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            // In real scenario, fetch customer email from auth-service
            // For now log it — wire up with actual email when SMTP is configured
            mail.setTo("customer-" + message.customerId() + "@smartcourier.local");
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            log.info("✅ Email sent for delivery={} event={}", message.deliveryId(), message.eventCode());
        } catch (Exception ex) {
            log.warn("⚠️ Email send failed for delivery={}: {}", message.deliveryId(), ex.getMessage());
        }
    }

    private String buildSubject(DeliveryEventMessage msg) {
        return switch (msg.eventCode()) {
            case "CREATED"          -> "SmartCourier: Shipment Created — " + msg.trackingNumber();
            case "BOOKED"           -> "SmartCourier: Shipment Confirmed — " + msg.trackingNumber();
            case "PICKED_UP"        -> "SmartCourier: Parcel Picked Up — " + msg.trackingNumber();
            case "IN_TRANSIT"       -> "SmartCourier: Parcel In Transit — " + msg.trackingNumber();
            case "OUT_FOR_DELIVERY" -> "SmartCourier: Out for Delivery — " + msg.trackingNumber();
            case "DELIVERED"        -> "SmartCourier: Delivered! — " + msg.trackingNumber();
            case "CANCELLED"        -> "SmartCourier: Shipment Cancelled — " + msg.trackingNumber();
            default                 -> "SmartCourier: Update — " + msg.trackingNumber();
        };
    }

    private String buildBody(DeliveryEventMessage msg) {
        return switch (msg.eventCode()) {
            case "CREATED" -> String.format(
                "Your shipment has been created.\n\nTracking: %s\nTo: %s\nService: %s\n\nTrack at: http://localhost:5174/track/%s",
                msg.trackingNumber(), msg.destinationAddress(), msg.serviceType(), msg.trackingNumber());
            case "BOOKED" -> String.format(
                "Your shipment is confirmed and awaiting pickup.\n\nTracking: %s\nReceiver: %s\nDestination: %s",
                msg.trackingNumber(), msg.receiverName(), msg.destinationAddress());
            case "PICKED_UP" -> String.format(
                "Great news! Your parcel has been picked up and is on its way.\n\nTracking: %s",
                msg.trackingNumber());
            case "IN_TRANSIT" -> String.format(
                "Your parcel is in transit to %s.\n\nTracking: %s",
                msg.destinationAddress(), msg.trackingNumber());
            case "OUT_FOR_DELIVERY" -> String.format(
                "Your parcel is out for delivery to %s. Expect it today!\n\nTracking: %s",
                msg.receiverName(), msg.trackingNumber());
            case "DELIVERED" -> String.format(
                "Your parcel has been delivered to %s at %s.\n\nTracking: %s\n\nThank you for using SmartCourier!",
                msg.receiverName(), msg.destinationAddress(), msg.trackingNumber());
            case "CANCELLED" -> String.format(
                "Your shipment %s has been cancelled.\n\nIf this was unexpected, please contact support.",
                msg.trackingNumber());
            default -> String.format("Update for shipment %s: %s", msg.trackingNumber(), msg.description());
        };
    }
}
