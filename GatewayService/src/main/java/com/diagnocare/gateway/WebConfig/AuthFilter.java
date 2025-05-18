package com.diagnocare.gateway.WebConfig;

import com.diagnocare.gateway.Dto.UserDto;
import com.diagnocare.gateway.config.AuthServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth.service.uri}")
    private String authServiceUri;

    @Value("${auth.service.validate.token.endpoint}")
    private String authServiceValidateTokenEndpoint;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return createErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid Authorization header format");
            }

            String token = authHeader.substring(7); // Remove "Bearer "

            log.debug("Validating token for request to {}", exchange.getRequest().getURI());

            return webClientBuilder.build()
                    .post()
                    .uri(authServiceUri + authServiceValidateTokenEndpoint )
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.equals(HttpStatus.UNAUTHORIZED),
                            response -> response.bodyToMono(String.class)
                                    .defaultIfEmpty("Invalid or expired token")
                                    .flatMap(body -> Mono.error(new AuthServiceException(HttpStatus.UNAUTHORIZED, body)))
                    )
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            response -> response.bodyToMono(String.class)
                                    .defaultIfEmpty("Client error during token validation")
                                    .flatMap(body -> {
                                        HttpStatus status = toHttpStatus(response.statusCode());
                                        return Mono.error(new AuthServiceException(status, body));
                                    })
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            response -> response.bodyToMono(String.class)
                                    .defaultIfEmpty("Auth service error")
                                    .flatMap(body -> {
                                        HttpStatus status = toHttpStatus(response.statusCode());
                                        return Mono.error(new AuthServiceException(status, body));
                                    })
                    )
                    .bodyToMono(UserDto.class)
                    .flatMap(userDto -> {
                        log.debug("Token validation successful for user {} in request to {}",
                                userDto.getEmail(), exchange.getRequest().getURI());

                        var mutatedRequest = exchange.getRequest().mutate()
                                .header("x-auth-user-id", String.valueOf(userDto.getId()))
                                .header("x-auth-user-email", userDto.getEmail())
                                .header("x-auth-user-role", userDto.getRole())
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    })
                    .onErrorResume(e -> {
                        if (e instanceof AuthServiceException authEx) {
                            return createErrorResponse(exchange, authEx.getStatus(), authEx.getMessage());
                        }
                        log.error("Unexpected error during token validation", e);
                        return createErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
                    });
        };
    }

    private HttpStatus toHttpStatus(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return (status != null) ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Mono<Void> createErrorResponse(org.springframework.web.server.ServerWebExchange exchange,
                                           HttpStatus status, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> responseBodyMap = new HashMap<>();
        responseBodyMap.put("status", status.value());
        responseBodyMap.put("message", message);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBodyMap);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    public static class Config {
        // Add config fields if needed later
    }
}
