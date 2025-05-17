package com.diagnocare.gateway.WebConfig;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        System.out.println(ex.getMessage());
        
        if (message == null || message.isEmpty()) {
            message = "Invalid or expired token";
        }
        
        if (message.contains("Invalid or expired token") || 
            message.contains("Missing Authorization header") || 
            message.contains("Invalid Authorization header format")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("Token validation failed")) {
            status = HttpStatus.BAD_REQUEST;
        }
        
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String responseBody = String.format("{\"status\": %d, \"message\": \"%s\"}", 
            status.value(), message);
            
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(responseBody.getBytes())));
    }
} 