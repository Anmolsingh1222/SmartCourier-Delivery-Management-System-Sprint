package com.smartcourier.delivery.service;

import com.smartcourier.delivery.domain.DeliveryEntity;
import com.smartcourier.delivery.domain.DeliveryStatus;
import com.smartcourier.delivery.repository.DeliveryRepository;
import com.smartcourier.delivery.web.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.web.dto.DeliveryResponse;
import com.smartcourier.delivery.web.dto.PickupScheduleRequest;
import com.smartcourier.delivery.web.dto.PriceEstimateRequest;
import com.smartcourier.delivery.web.dto.UpdateDeliveryRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService implements DeliveryServicePort {

    private final DeliveryRepository deliveryRepository;
    private final TrackingEventPublisherPort trackingEventPublisher;
    private final SecureRandom random = new SecureRandom();

    public DeliveryService(DeliveryRepository deliveryRepository, TrackingEventPublisherPort trackingEventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.trackingEventPublisher = trackingEventPublisher;
    }

    @Override
    @Transactional
    public DeliveryResponse create(Long customerId, CreateDeliveryRequest request) {
        DeliveryEntity entity = new DeliveryEntity();
        entity.setTrackingNumber(generateTrackingNumber());
        entity.setCustomerId(customerId);
        entity.setStatus(DeliveryStatus.DRAFT);
        entity.setSenderName(request.senderName().trim());
        entity.setReceiverName(request.receiverName().trim());
        entity.setReceiverPhone(request.receiverPhone().trim());
        entity.setPickupAddress(request.pickupAddress().trim());
        entity.setDestinationAddress(request.destinationAddress().trim());
        entity.setPackageWeightKg(request.packageWeightKg());
        entity.setPackageType(request.packageType().trim());
        entity.setServiceType(request.serviceType().trim());
        entity.setQuotedPrice(estimatePriceValue(request.packageWeightKg(), request.serviceType()));
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, "CREATED", "Delivery request created");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> my(Long customerId) {
        return deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse details(Long id, Long customerId, String role) {
        DeliveryEntity entity = findOrThrow(id);
        assertCanRead(entity, customerId, role);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public DeliveryResponse update(Long id, Long customerId, String role, UpdateDeliveryRequest request) {
        DeliveryEntity entity = findOrThrow(id);
        assertCanModifyCustomerDraft(entity, customerId, role);

        entity.setReceiverName(request.receiverName().trim());
        entity.setReceiverPhone(request.receiverPhone().trim());
        entity.setDestinationAddress(request.destinationAddress().trim());
        entity.setPackageWeightKg(request.packageWeightKg());
        entity.setPackageType(request.packageType().trim());
        entity.setServiceType(request.serviceType().trim());
        entity.setQuotedPrice(estimatePriceValue(request.packageWeightKg(), request.serviceType()));
        return toResponse(deliveryRepository.save(entity));
    }

    @Override
    @Transactional
    public Map<String, String> delete(Long id, Long customerId, String role) {
        DeliveryEntity entity = findOrThrow(id);
        assertCanModifyCustomerDraft(entity, customerId, role);
        deliveryRepository.delete(entity);
        return Map.of("message", "Delivery deleted");
    }

    @Override
    @Transactional
    public DeliveryResponse book(Long id, Long customerId, String role) {
        DeliveryEntity entity = findOrThrow(id);
        assertCustomerOwner(entity, customerId, role);
        if (entity.getStatus() != DeliveryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT delivery can be booked");
        }
        entity.setStatus(DeliveryStatus.BOOKED);
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, "BOOKED", "Delivery booked");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse schedulePickup(Long id, Long customerId, String role, PickupScheduleRequest request) {
        DeliveryEntity entity = findOrThrow(id);
        assertCustomerOwner(entity, customerId, role);
        if (entity.getStatus() != DeliveryStatus.BOOKED) {
            throw new IllegalStateException("Pickup can be scheduled only after booking");
        }
        entity.setPickupSlot(request.pickupSlot().trim());
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, "PICKUP_SCHEDULED", "Pickup scheduled: " + saved.getPickupSlot());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse reschedulePickup(Long id, Long customerId, String role, PickupScheduleRequest request) {
        DeliveryEntity entity = findOrThrow(id);
        assertCustomerOwner(entity, customerId, role);
        if (entity.getStatus() != DeliveryStatus.BOOKED) {
            throw new IllegalStateException("Pickup can be rescheduled only after booking");
        }
        entity.setPickupSlot(request.pickupSlot().trim());
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, "PICKUP_RESCHEDULED", "Pickup rescheduled: " + saved.getPickupSlot());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeliveryResponse cancel(Long id, Long customerId, String role) {
        DeliveryEntity entity = findOrThrow(id);
        assertCustomerOwner(entity, customerId, role);
        if (entity.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Delivered parcel cannot be canceled");
        }
        entity.setStatus(DeliveryStatus.CANCELLED);
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, "CANCELLED", "Delivery canceled");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> price(Long id, Long customerId, String role) {
        DeliveryEntity entity = findOrThrow(id);
        assertCanRead(entity, customerId, role);
        return Map.of("deliveryId", entity.getId(), "trackingNumber", entity.getTrackingNumber(), "quotedPrice", entity.getQuotedPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> estimate(PriceEstimateRequest request) {
        BigDecimal value = estimatePriceValue(request.packageWeightKg(), request.serviceType());
        return Map.of("estimatedPrice", value, "currency", "INR");
    }

    @Override
    @Transactional
    public DeliveryResponse markPickedUp(Long id, String role) { return transitionByAdmin(id, role, DeliveryStatus.BOOKED, DeliveryStatus.PICKED_UP, "PICKED_UP", "Parcel picked up"); }
    @Override
    @Transactional
    public DeliveryResponse markInTransit(Long id, String role) { return transitionByAdmin(id, role, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT, "IN_TRANSIT", "Parcel in transit"); }
    @Override
    @Transactional
    public DeliveryResponse markOutForDelivery(Long id, String role) { return transitionByAdmin(id, role, DeliveryStatus.IN_TRANSIT, DeliveryStatus.OUT_FOR_DELIVERY, "OUT_FOR_DELIVERY", "Parcel out for delivery"); }
    @Override
    @Transactional
    public DeliveryResponse markDelivered(Long id, String role) { return transitionByAdmin(id, role, DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.DELIVERED, "DELIVERED", "Parcel delivered"); }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> search(String q, String status, String role, Long customerId) {
        if (isAdmin(role)) {
            if (status != null && !status.isBlank()) {
                return deliveryRepository.findByStatusOrderByUpdatedAtDesc(DeliveryStatus.valueOf(status.toUpperCase())).stream().map(this::toResponse).toList();
            }
            return deliveryRepository.findAll().stream().map(this::toResponse).toList();
        }

        List<DeliveryEntity> mine = deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (q == null || q.isBlank()) {
            return mine.stream().map(this::toResponse).toList();
        }
        String needle = q.toLowerCase();
        return mine.stream()
                .filter(d -> d.getTrackingNumber().toLowerCase().contains(needle)
                        || d.getReceiverName().toLowerCase().contains(needle))
                .map(this::toResponse)
                .toList();
    }

    private DeliveryResponse toResponse(DeliveryEntity e) {
        return new DeliveryResponse(
                e.getId(), e.getTrackingNumber(), e.getCustomerId(), e.getStatus().name(),
                e.getSenderName(), e.getReceiverName(), e.getReceiverPhone(), e.getPickupAddress(),
                e.getDestinationAddress(), e.getPackageWeightKg(), e.getPackageType(), e.getServiceType(),
                e.getQuotedPrice(), e.getPickupSlot(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private DeliveryResponse transitionByAdmin(
            Long id, String role, DeliveryStatus from, DeliveryStatus to, String eventCode, String description) {
        if (!isAdmin(role)) {
            throw new AccessDeniedException("Admin role required");
        }
        DeliveryEntity entity = findOrThrow(id);
        if (entity.getStatus() != from) {
            throw new IllegalStateException("Invalid transition from " + entity.getStatus() + " to " + to);
        }
        entity.setStatus(to);
        DeliveryEntity saved = deliveryRepository.save(entity);
        trackingEventPublisher.publish(saved, eventCode, description);
        return toResponse(saved);
    }

    private DeliveryEntity findOrThrow(Long id) {
        return deliveryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
    }

    private void assertCanRead(DeliveryEntity entity, Long customerId, String role) {
        if (isAdmin(role)) {
            return;
        }
        if (!entity.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You cannot access this delivery");
        }
    }

    private void assertCanModifyCustomerDraft(DeliveryEntity entity, Long customerId, String role) {
        assertCustomerOwner(entity, customerId, role);
        if (entity.getStatus() != DeliveryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT deliveries can be modified by customer");
        }
    }

    private void assertCustomerOwner(DeliveryEntity entity, Long customerId, String role) {
        if (isAdmin(role)) {
            return;
        }
        if (!entity.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You cannot modify this delivery");
        }
    }

    private boolean isAdmin(String role) {
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    private BigDecimal estimatePriceValue(BigDecimal weight, String serviceType) {
        BigDecimal base = switch ((serviceType == null ? "STANDARD" : serviceType).toUpperCase()) {
            case "EXPRESS" -> new BigDecimal("180");
            case "INTERNATIONAL" -> new BigDecimal("450");
            default -> new BigDecimal("120");
        };
        return base.add(weight.multiply(new BigDecimal("18"))).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateTrackingNumber() {
        return "SC" + Instant.now().toEpochMilli() + (100 + random.nextInt(900));
    }
}
