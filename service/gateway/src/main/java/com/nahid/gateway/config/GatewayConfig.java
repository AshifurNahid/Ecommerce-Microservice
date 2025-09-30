package com.nahid.gateway.config;

import com.nahid.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes - Apply JWT filter
                .route("user-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://user-service"))

                // Product Service Routes - Apply JWT filter
                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3)) // Add retry for resilience
                        .uri("lb://product-service"))

                // Category Service Routes - Apply JWT filter
                .route("product-service-categories", r -> r
                        .path("/api/v1/categories/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3))
                        .uri("lb://product-service"))

                // Order Service Routes - Apply JWT filter
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3))
                        .uri("lb://order-service"))

                // Payment Service Routes - Apply JWT filter
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3))
                        .uri("lb://payment-service"))

                .build();
    }
}
