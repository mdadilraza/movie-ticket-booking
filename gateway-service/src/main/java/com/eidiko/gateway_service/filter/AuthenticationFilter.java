package com.eidiko.gateway_service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    // List of endpoints to skip authentication
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            log.info("Request path: {}", path);

            // Skip authentication for open endpoints
            if (OPEN_ENDPOINTS.contains(path)) {
                log.info("Skipping authentication for open endpoint: {}", path);
                return chain.filter(exchange);
            }

            // Check for Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid or missing Authorization header for path: {}", path);
                return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
            }

            // Extract token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            log.info("Validating token for path: {}", path);

            // Call external auth validation endpoint
            return webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8081/auth/validate")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(response -> {
                        log.info("Token validated successfully, username: {}", response);
                        // Add username to the request headers
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("loggedInUser", response)
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(e -> {
                        log.error("Token validation failed for path: {}, error: {}", path, e.getMessage());
                        return unauthorizedResponse(exchange, "Token validation failed: " + e.getMessage());
                    });
        };
    }

    public static class Config {}

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Set response body
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}