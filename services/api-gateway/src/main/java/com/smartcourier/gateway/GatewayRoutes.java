package com.smartcourier.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-docs", r -> r.path("/gateway/auth/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://AUTH-SERVICE"))
                .route("delivery-service-docs", r -> r.path("/gateway/deliveries/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://DELIVERY-SERVICE"))
                .route("tracking-service-docs", r -> r.path("/gateway/tracking/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://TRACKING-SERVICE"))
                .route("admin-service-docs", r -> r.path("/gateway/admin/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://ADMIN-SERVICE"))
                .route("auth-service-direct-api", r -> r.path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE"))
                .route("delivery-service-direct-api", r -> r.path("/api/deliveries/**")
                        .uri("lb://DELIVERY-SERVICE"))
                .route("tracking-service-direct-api", r -> r.path("/api/tracking/**")
                        .uri("lb://TRACKING-SERVICE"))
                .route("admin-service-direct-api", r -> r.path("/api/admin/**")
                        .uri("lb://ADMIN-SERVICE"))
                .route("delivery-service-root", r -> r.path("/gateway/deliveries")
                        .filters(f -> f.setPath("/api/deliveries"))
                        .uri("lb://DELIVERY-SERVICE"))
                .route("auth-service", r -> r.path("/gateway/auth/**")
                        .filters(f -> f.rewritePath("/gateway/auth/(?<segment>.*)", "/api/auth/${segment}"))
                        .uri("lb://AUTH-SERVICE"))
                .route("delivery-service", r -> r.path("/gateway/deliveries/**")
                        .filters(f -> f.rewritePath("/gateway/deliveries/(?<segment>.*)", "/api/deliveries/${segment}"))
                        .uri("lb://DELIVERY-SERVICE"))
                .route("tracking-service", r -> r.path("/gateway/tracking/**")
                        .filters(f -> f.rewritePath("/gateway/tracking/(?<segment>.*)", "/api/tracking/${segment}"))
                        .uri("lb://TRACKING-SERVICE"))
                .route("admin-service", r -> r.path("/gateway/admin/**")
                        .filters(f -> f.rewritePath("/gateway/admin/(?<segment>.*)", "/api/admin/${segment}"))
                        .uri("lb://ADMIN-SERVICE"))
                .build();
    }
}
