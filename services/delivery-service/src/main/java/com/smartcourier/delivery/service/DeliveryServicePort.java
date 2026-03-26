package com.smartcourier.delivery.service;

import com.smartcourier.delivery.web.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.web.dto.DeliveryResponse;
import com.smartcourier.delivery.web.dto.PickupScheduleRequest;
import com.smartcourier.delivery.web.dto.PriceEstimateRequest;
import com.smartcourier.delivery.web.dto.UpdateDeliveryRequest;
import java.util.List;
import java.util.Map;

public interface DeliveryServicePort {

    DeliveryResponse create(Long customerId, CreateDeliveryRequest request);

    List<DeliveryResponse> my(Long customerId);

    DeliveryResponse details(Long id, Long customerId, String role);

    DeliveryResponse update(Long id, Long customerId, String role, UpdateDeliveryRequest request);

    Map<String, String> delete(Long id, Long customerId, String role);

    DeliveryResponse book(Long id, Long customerId, String role);

    DeliveryResponse schedulePickup(Long id, Long customerId, String role, PickupScheduleRequest request);

    DeliveryResponse reschedulePickup(Long id, Long customerId, String role, PickupScheduleRequest request);

    DeliveryResponse cancel(Long id, Long customerId, String role);

    Map<String, Object> price(Long id, Long customerId, String role);

    Map<String, Object> estimate(PriceEstimateRequest request);

    DeliveryResponse markPickedUp(Long id, String role);

    DeliveryResponse markInTransit(Long id, String role);

    DeliveryResponse markOutForDelivery(Long id, String role);

    DeliveryResponse markDelivered(Long id, String role);

    List<DeliveryResponse> search(String q, String status, String role, Long customerId);
}
