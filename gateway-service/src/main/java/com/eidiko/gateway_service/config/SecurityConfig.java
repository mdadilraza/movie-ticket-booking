package com.eidiko.gateway_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//@EnableWebSecurity
public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/actuator/**").permitAll() // Allow actuator if needed
//                        .anyExchange().permitAll() // Gateway routes handle security
//                );
//        return http.build();
//    }
//    @Bean
//    public WebClient builder(){
//        return WebClient.builder().build();
//    }
}
