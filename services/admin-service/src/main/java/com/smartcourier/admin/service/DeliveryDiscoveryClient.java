package com.smartcourier.admin.service;

import com.smartcourier.admin.web.dto.AdminDeliveryView;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DeliveryDiscoveryClient implements DeliveryQueryPort {

    private static final Logger log = LoggerFactory.getLogger(DeliveryDiscoveryClient.class);
    private static final String DELIVERY_SERVICE_ID = "DELIVERY-SERVICE";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ADMIN_USER_ID = "0";

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    public DeliveryDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.restClient = RestClient.create();
    }

    @Override
    public List<AdminDeliveryView> findAllDeliveriesForAdmin() {
        List<ServiceInstance> instances = discoveryClient.getInstances(DELIVERY_SERVICE_ID);
        if (instances == null || instances.isEmpty()) {
            log.warn("Delivery service not found in Eureka registry; returning empty list");
            return List.of();
        }

        ServiceInstance instance = instances.get(0);
        try {
            AdminDeliveryView[] deliveries = restClient.get()
                    .uri(instance.getUri() + "/api/deliveries/search")
                    .header("X-User-Id", ADMIN_USER_ID)
                    .header("X-User-Role", ADMIN_ROLE)
                    .retrieve()
                    .body(AdminDeliveryView[].class);

            if (deliveries == null || deliveries.length == 0) {
                return List.of();
            }
            return Arrays.asList(deliveries);
        } catch (RestClientException ex) {
            log.error("Failed to fetch deliveries from delivery-service at {}: {}", instance.getUri(), ex.getMessage());
            return List.of();
        }
    }
}
