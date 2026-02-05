package com.homosapiens.diagnocareservice.core.exception;

import com.homosapiens.diagnocareservice.core.exception.entity.CustomResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<CustomResponseEntity> handleAppException(AppException ex, HttpServletRequest request) {
        String lang = resolveLang(request);
        CustomResponseEntity response = CustomResponseEntity.builder()
                .statusCode(ex.getStatus().value())
                .message(translateMessage(ex.getMessage(), lang))
                .data(null)
                .build();
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponseEntity> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String lang = resolveLang(request);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        CustomResponseEntity response = CustomResponseEntity.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(translateMessage("Validation failed", lang))
                .data(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponseEntity> handleGenericException(Exception ex, HttpServletRequest request) {
        String lang = resolveLang(request);
        CustomResponseEntity response = CustomResponseEntity.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(translateMessage("Internal Server Error", lang))
                .data(translateMessage(ex.getMessage(), lang))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String resolveLang(HttpServletRequest request) {
        if (request == null) {
            return "fr";
        }
        String headerLang = request.getHeader("x-auth-user-lang");
        if (headerLang != null && headerLang.toLowerCase().startsWith("en")) {
            return "en";
        }
        String acceptLang = request.getHeader("Accept-Language");
        if (acceptLang != null && acceptLang.toLowerCase().startsWith("en")) {
            return "en";
        }
        return "fr";
    }

    private String translateMessage(String message, String lang) {
        if (message == null || "en".equals(lang)) {
            return message;
        }
        return switch (message) {
            case "Validation failed" -> "Validation échouée";
            case "Validation Error" -> "Erreur de validation";
            case "Internal Server Error" -> "Erreur interne du serveur";
            case "User not found" -> "Utilisateur introuvable";
            default -> message;
        };
    }
}