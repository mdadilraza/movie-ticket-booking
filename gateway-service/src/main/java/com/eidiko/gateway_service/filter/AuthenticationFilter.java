package com.eidiko.gateway_service.filter;

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
import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return unauthorizedResponse(exchange, "Invalid Authorization header format");
                }

                String token = authHeader.substring(7); // Remove "Bearer " prefix

                // Call external auth validation endpoint
                return webClientBuilder.build()
                        .post()
                        .uri("http://localhost:8081/auth/validate")
                        .bodyValue(Map.of("token",token))
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(response -> {
                            // Extract username from the response (assuming it's just a string)
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("loggedInUser", response)
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        })
                        .onErrorResume(e -> {
                            e.printStackTrace(); // Log error
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
