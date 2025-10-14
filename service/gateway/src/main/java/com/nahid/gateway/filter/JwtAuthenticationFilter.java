package com.nahid.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nahid.gateway.config.GatewaySecurityProperties;
import com.nahid.gateway.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Global filter that validates JWT tokens before forwarding requests to downstream services.
 * <p>
 * The filter is applied to every request while respecting the configured public endpoints. On a
 * successful validation the authenticated user context is propagated via headers so that
 * microservices can rely on the gateway for authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_EMAIL = "X-Auth-User";
    private static final String HEADER_USER_ROLES = "X-Auth-Roles";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final GatewaySecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();

        if (isPublicEndpoint(requestPath)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request.getHeaders());
        if (!StringUtils.hasText(token)) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        try {
            if (!jwtUtil.isTokenValid(token)) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid JWT token");
            }

            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HEADER_USER_EMAIL, username)
                    .header(HEADER_USER_ROLES, roles == null ? "" : String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token expired for path {}: {}", requestPath, ex.getMessage());
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "JWT token expired");
        } catch (JwtException ex) {
            log.error("JWT validation failed for path {}: {}", requestPath, ex.getMessage());
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "JWT validation failed");
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(String path) {
        return securityProperties.getPublicEndpoints().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractToken(HttpHeaders headers) {
        List<String> authorizationHeaders = headers.getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authorizationHeaders)) {
            return null;
        }

        String authorization = authorizationHeaders.get(0);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> responseBody = Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", exchange.getRequest().getURI().getPath()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise error response: {}", e.getMessage());
            byte[] fallback = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }
}

