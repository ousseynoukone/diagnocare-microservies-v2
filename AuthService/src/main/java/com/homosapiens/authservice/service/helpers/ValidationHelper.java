package com.homosapiens.authservice.service.helpers;

import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collectors;

public class ValidationHelper {

    public static ResponseEntity<CustomResponseEntity> buildValidationReponse(BindingResult bindingResult) {

        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        CustomResponseEntity errorResponse = CustomResponseEntity.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .message("Validation failed")
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
