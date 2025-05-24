package com.diagnocare.gateway.core.config.webConfig.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        if (throwable instanceof NotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String errorMessage = extractServiceNameFromMessage(throwable.getMessage());

            HttpError errorResponse = new HttpError(errorMessage);

            try {
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                DataBuffer dataBuffer = bufferFactory.wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(dataBuffer));
            } catch (JsonProcessingException e) {
                DataBuffer fallback = bufferFactory.wrap("{\"error\": \"Service unavailable\"}".getBytes());
                return exchange.getResponse().writeWith(Mono.just(fallback));
            }
        }

        // Default error handler
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        DataBuffer dataBuffer = bufferFactory.wrap("Unknown error".getBytes());
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private String extractServiceNameFromMessage(String message) {
        String prefix = "Unable to find instance for ";
        int index = message.indexOf(prefix);
        if (index >= 0) {
            String serviceName = message.substring(index + prefix.length()).replace("\"", "");
            return "Service " + serviceName + " is temporarily unavailable. Please try again later.";
        }
        return "Service is temporarily unavailable. Please try again later.";
    }

    public static class HttpError {
        private String error;

        public HttpError(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
