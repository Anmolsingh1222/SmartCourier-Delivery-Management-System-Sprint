package com.smartcourier.delivery;

import com.smartcourier.delivery.service.DeliveryServicePort;
import com.smartcourier.delivery.service.RequestContextService;
import com.smartcourier.delivery.web.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.web.dto.DeliveryResponse;
import com.smartcourier.delivery.web.dto.PickupScheduleRequest;
import com.smartcourier.delivery.web.dto.PriceEstimateRequest;
import com.smartcourier.delivery.web.dto.UpdateDeliveryRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryServicePort deliveryService;
    private final RequestContextService requestContextService;

    public DeliveryController(DeliveryServicePort deliveryService, RequestContextService requestContextService) {
        this.deliveryService = deliveryService;
        this.requestContextService = requestContextService;
    }

    @PostMapping
    public DeliveryResponse create(
            @RequestHeader("X-User-Id") String userIdHeader,
            @Valid @RequestBody CreateDeliveryRequest request) {
        return deliveryService.create(requestContextService.userId(userIdHeader), request);
    }

    @GetMapping("/my")
    public List<DeliveryResponse> myDeliveries(@RequestHeader("X-User-Id") String userIdHeader) {
        return deliveryService.my(requestContextService.userId(userIdHeader));
    }

    @GetMapping("/{id}")
    public DeliveryResponse details(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.details(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader));
    }

    @PutMapping("/{id}")
    public DeliveryResponse update(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @Valid @RequestBody UpdateDeliveryRequest request) {
        return deliveryService.update(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader), request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.delete(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader));
    }

    @PostMapping("/{id}/book")
    public DeliveryResponse book(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.book(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader));
    }

    @PostMapping("/{id}/pickup/schedule")
    public DeliveryResponse schedulePickup(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @Valid @RequestBody PickupScheduleRequest request) {
        return deliveryService.schedulePickup(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader), request);
    }

    @PutMapping("/{id}/pickup/reschedule")
    public DeliveryResponse reschedulePickup(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @Valid @RequestBody PickupScheduleRequest request) {
        return deliveryService.reschedulePickup(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader), request);
    }

    @PostMapping("/{id}/cancel")
    public DeliveryResponse cancel(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.cancel(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader));
    }

    @GetMapping("/{id}/price")
    public Map<String, Object> price(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.price(id, requestContextService.userId(userIdHeader), requestContextService.role(roleHeader));
    }

    @PostMapping("/estimate")
    public Map<String, Object> estimate(@Valid @RequestBody PriceEstimateRequest request) {
        return deliveryService.estimate(request);
    }

    @PostMapping("/{id}/status/picked-up")
    public DeliveryResponse pickedUp(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.markPickedUp(id, requestContextService.role(roleHeader));
    }

    @PostMapping("/{id}/status/in-transit")
    public DeliveryResponse inTransit(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.markInTransit(id, requestContextService.role(roleHeader));
    }

    @PostMapping("/{id}/status/out-for-delivery")
    public DeliveryResponse outForDelivery(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.markOutForDelivery(id, requestContextService.role(roleHeader));
    }

    @PostMapping("/{id}/status/delivered")
    public DeliveryResponse delivered(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.markDelivered(id, requestContextService.role(roleHeader));
    }

    @GetMapping("/search")
    public List<DeliveryResponse> search(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader) {
        return deliveryService.search(q, status, requestContextService.role(roleHeader), requestContextService.userId(userIdHeader));
    }
}
