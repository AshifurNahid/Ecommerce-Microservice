package com.nahid.gateway.filter;

import com.nahid.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter for Spring Cloud Gateway
 * This filter validates JWT tokens for secured routes
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.security.public-endpoints}")
    private List<String> publicEndpoints;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("Processing request to path: {}", path);

            // Check if the path is public (no authentication required)
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint accessed: {}", path);
                return chain.filter(exchange);
            }

            // Extract JWT token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            try {
                // Validate the token
                if (!jwtUtil.isTokenValid(token)) {
                    log.warn("Invalid JWT token for path: {}", path);
                    return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user information from token
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                log.debug("Authenticated user: {} with roles: {}", username, roles);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Email", username)
                        .header("X-User-Roles", String.join(",", roles))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT validation error: {}", e.getMessage());
                return onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Check if the endpoint is public (no authentication required)
     */
    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(pattern -> {
            // Convert pattern to regex (e.g., /api/auth/** -> /api/auth/.*)
            String regex = pattern.replace("**", ".*").replace("*", "[^/]*");
            return path.matches(regex);
        });
    }

    /**
     * Handle errors by returning appropriate HTTP response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
                httpStatus.getReasonPhrase(),
                errorMessage,
                httpStatus.value()
        );

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        // You can add configuration properties here if needed
    }
}
