package com.diagnocare.gateway.core.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable);
//            .authorizeExchange(exchanges -> exchanges
//                .pathMatchers("/api/auth/v1/login",
//                            "/api/auth/v1/register",
//                            "/api/auth/v1/validate-token",
//                            "/api/auth/v1/refresh",
//                            "/swagger-ui/**",
//                            "/v3/api-docs/**").permitAll()
//                .anyExchange().authenticated()
//            );
        return http.build();
    }


}