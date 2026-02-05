package com.homosapiens.diagnocareservice.core.response;

import com.homosapiens.diagnocareservice.core.exception.entity.CustomResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (isSwaggerPath(request)) {
            return body;
        }
        if (body instanceof CustomResponseEntity) {
            return body;
        }

        int statusCode = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse && servletResponse.getServletResponse() != null) {
            statusCode = servletResponse.getServletResponse().getStatus();
        } else if (response.getStatusCode() != null) {
            statusCode = response.getStatusCode().value();
        }

        String lang = resolveLang(request);
        String message = "en".equals(lang) ? "Success" : "Succès";

        return CustomResponseEntity.builder()
                .statusCode(statusCode)
                .message(message)
                .data(body)
                .build();
    }

    private boolean isSwaggerPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.contains("/swagger-ui") || path.contains("/v3/api-docs");
    }

    private String resolveLang(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest http = servletRequest.getServletRequest();
            String headerLang = http.getHeader("x-auth-user-lang");
            if (headerLang != null && headerLang.toLowerCase().startsWith("en")) {
                return "en";
            }
            String acceptLang = http.getHeader("Accept-Language");
            if (acceptLang != null && acceptLang.toLowerCase().startsWith("en")) {
                return "en";
            }
        }
        return "fr";
    }
}
