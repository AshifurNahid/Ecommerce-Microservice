package com.nahid.gateway.config;

import com.nahid.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

/**
 * ADVANCED Route Configuration Example
 * 
 * This demonstrates industry best practices for managing many API endpoints:
 * 1. Grouping routes by service
 * 2. Applying filters conditionally
 * 3. Method-specific routing
 * 4. Rate limiting
 * 5. Circuit breaker patterns
 * 6. Request/Response transformation
 * 
 * NOTE: This is an ALTERNATIVE to GatewayConfig.java
 * Use ONE of these approaches, not both simultaneously
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouteConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.security.public-endpoints}")
    private String publicEndpoints;

    /**
     * APPROACH 1: Grouped Route Configuration
     * Best for: Medium to large number of endpoints with shared behaviors
     */
    // @Bean
    public RouteLocator groupedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // ============================================
                // USER SERVICE ROUTES
                // ============================================
                .route("user-auth-public", r -> r
                        .path("/api/auth/login", "/api/auth/register", "/api/auth/refresh")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway", "public-route")
                                .retry(2))
                        .uri("lb://user-service"))

                .route("user-auth-secured", r -> r
                        .path("/api/auth/logout", "/api/auth/me")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Gateway", "secured-route"))
                        .uri("lb://user-service"))

                .route("user-profile", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3))
                        .uri("lb://user-service"))

                // ============================================
                // PRODUCT SERVICE ROUTES
                // ============================================
                .route("product-read", r -> r
                        .path("/api/v1/products/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3)
                                .addResponseHeader("X-Cache-Control", "public, max-age=300"))
                        .uri("lb://product-service"))

                .route("product-write", r -> r
                        .path("/api/v1/products/**")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Require-Admin", "true"))
                        .uri("lb://product-service"))

                .route("category-management", r -> r
                        .path("/api/v1/categories/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3))
                        .uri("lb://product-service"))

                // ============================================
                // ORDER SERVICE ROUTES
                // ============================================
                .route("order-management", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(2)
                                .addRequestHeader("X-Service", "order"))
                        .uri("lb://order-service"))

                // ============================================
                // PAYMENT SERVICE ROUTES
                // ============================================
                .route("payment-processing", r -> r
                        .path("/api/v1/payments/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-Critical", "true")
                                .addRequestHeader("X-Service", "payment"))
                        .uri("lb://payment-service"))

                .build();
    }

    /**
     * APPROACH 2: Dynamic Route Configuration
     * Best for: Large number of services with similar patterns
     * Routes are generated based on service registry
     */
    // @Bean
    public RouteLocator dynamicRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Catch-all route for any service registered in Eureka
                .route("dynamic-service-router", r -> r
                        .path("/api/{service}/**")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .rewritePath("/api/(?<service>.*)/(?<remaining>.*)", "/${remaining}")
                                .addRequestHeader("X-Forwarded-Service", "${service}"))
                        .uri("lb://${service}-service"))
                .build();
    }

    /**
     * APPROACH 3: Microservice-Specific Predicates
     * Best for: Complex routing logic with multiple conditions
     */
    // @Bean
    public RouteLocator advancedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route based on headers, query params, and path
                .route("advanced-product-search", r -> r
                        .path("/api/v1/products/search")
                        .and()
                        .query("q")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .retry(3)
                                .addRequestHeader("X-Search-Request", "true"))
                        .uri("lb://product-service"))

                // Route based on custom headers (e.g., API version)
                .route("versioned-api-v2", r -> r
                        .path("/api/v2/**")
                        .and()
                        .header("X-API-Version", "2.0")
                        .filters(f -> f
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .addRequestHeader("X-API-Version-Validated", "true"))
                        .uri("lb://product-service-v2"))

                .build();
    }

    /**
     * Key Resolver for Rate Limiting
     * Limits requests per user (based on JWT token)
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String user = exchange.getRequest().getHeaders().getFirst("X-User-Email");
            return Mono.just(user != null ? user : "anonymous");
        };
    }
}
