package com.smartcourier.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/gateway/auth/signup",
            "/gateway/auth/login",
            "/gateway/auth/refresh",
            "/gateway/auth/roles",
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/roles",
            "/gateway/services");

    private final JwtTokenValidator jwtTokenValidator;

    public JwtAuthFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if ((exchange.getRequest().getMethod() != null
                && "OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) || isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = authHeader.substring(7);
        try {
            var claims = jwtTokenValidator.validate(token);
            if (!"access".equals(claims.get("type"))) {
                return unauthorized(exchange, "Invalid token type");
            }
            if ((path.startsWith("/gateway/admin/") || path.startsWith("/api/admin/"))
                    && !String.valueOf(claims.get("role")).equalsIgnoreCase("ADMIN")) {
                return forbidden(exchange, "Admin role required");
            }

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(claims.getSubject()))
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::equals)
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/gateway/auth/v3/api-docs")
                || path.startsWith("/gateway/deliveries/v3/api-docs")
                || path.startsWith("/gateway/tracking/v3/api-docs")
                || path.startsWith("/gateway/admin/v3/api-docs")
                || path.startsWith("/gateway/auth/swagger-ui")
                || path.startsWith("/gateway/deliveries/swagger-ui")
                || path.startsWith("/gateway/tracking/swagger-ui")
                || path.startsWith("/gateway/admin/swagger-ui");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] payload = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(payload);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        byte[] payload = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(payload);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
