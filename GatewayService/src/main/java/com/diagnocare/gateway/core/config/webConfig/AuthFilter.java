package com.diagnocare.gateway.core.config.webConfig;

import com.diagnocare.gateway.data.dto.UserDto;
import com.diagnocare.gateway.core.config.exception.AuthServiceException;
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
            String path = exchange.getRequest().getURI().getPath();
            if (isSwaggerPath(path)) {
                return chain.filter(exchange);
            }
            if (isPublicAuthPath(path)) {
                return chain.filter(exchange);
            }
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
                                .header("x-auth-user-lang", userDto.getLang() != null ? userDto.getLang() : "fr")
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
        responseBodyMap.put("message", normalizeErrorMessage(message));

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBodyMap);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("Failed to serialize error response", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    /**
     * AuthService peut renvoyer des erreurs sous forme de JSON (ex: {"error":"..."}).
     * Si on met ce JSON brut dans "message", le client reçoit une string JSON dans une string.
     * Ici on essaie d'extraire un message lisible.
     */
    private String normalizeErrorMessage(String rawBody) {
        if (rawBody == null) {
            return "Unauthorized";
        }

        String trimmed = rawBody.trim();
        if (trimmed.isEmpty()) {
            return "Unauthorized";
        }

        // Cas le plus fréquent: {"error":"..."}
        // Ou parfois un wrapper: {"message":"Error","statusCode":401,"data":{"error":"..."}} etc.
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                var node = objectMapper.readTree(trimmed);

                if (node.hasNonNull("error") && node.get("error").isTextual()) {
                    return node.get("error").asText();
                }
                if (node.hasNonNull("message") && node.get("message").isTextual()) {
                    String msg = node.get("message").asText();
                    // Si c'est un "Error" générique, on tente de prendre un détail dans data.error
                    if (!msg.isBlank()) {
                        if (node.has("data") && node.get("data").hasNonNull("error") && node.get("data").get("error").isTextual()) {
                            return node.get("data").get("error").asText();
                        }
                        return msg;
                    }
                }
                if (node.has("data") && node.get("data").isTextual()) {
                    // Exemple: data contient déjà un message texte
                    String msg = node.get("data").asText();
                    if (!msg.isBlank()) {
                        return msg;
                    }
                }
            } catch (Exception ignore) {
                // Si parsing échoue, on retombe sur le rawBody
            }
        }

        // Sinon: c'est déjà un message texte.
        return trimmed;
    }

    public static class Config {
        // Add config fields if needed later
    }

    private boolean isSwaggerPath(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/v1/diagnocare/swagger-ui")
                || path.startsWith("/api/v1/diagnocare/v3/api-docs")
                || path.startsWith("/api/v1/diagnocare/swagger-ui.html");
    }

    /**
     * Endpoints that must be accessible without a JWT (login/register/refresh, etc.).
     * If these are protected by the gateway, callers will only see 401/403 instead of AuthService errors.
     */
    private boolean isPublicAuthPath(String path) {
        if (path == null) {
            return false;
        }
        // Adjust these prefixes to match your gateway route paths
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/register")
                || path.startsWith("/api/v1/auth/refresh")
                || path.startsWith("/api/v1/auth/validate")
                || path.startsWith("/api/v1/auth/swagger-ui")
                || path.startsWith("/api/v1/auth/v3/api-docs")
                || path.startsWith("/api/v1/auth/swagger-ui.html");
    }
}
