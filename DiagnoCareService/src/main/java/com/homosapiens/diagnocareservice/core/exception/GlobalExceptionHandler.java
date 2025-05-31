package com.homosapiens.diagnocareservice.core.exception;


import com.homosapiens.diagnocareservice.core.exception.entity.CustomResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new CustomResponseEntity( ex.getMessage(),ex.getStatus().value(),null));
    }
}
