package com.smartcourier.gateway;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayServicesController {

    @GetMapping("/services")
    public Map<String, Object> services() {
        return Map.of(
                "project", "SmartCourier Delivery Management System",
                "timestamp", Instant.now().toString(),
                "services", List.of(
                        Map.of("name", "AUTH-SERVICE", "path", "/gateway/auth/*", "status", "AVAILABLE"),
                        Map.of("name", "DELIVERY-SERVICE", "path", "/gateway/deliveries/*", "status", "AVAILABLE"),
                        Map.of("name", "TRACKING-SERVICE", "path", "/gateway/tracking/*", "status", "AVAILABLE"),
                        Map.of("name", "ADMIN-SERVICE", "path", "/gateway/admin/*", "status", "AVAILABLE")));
    }
}
