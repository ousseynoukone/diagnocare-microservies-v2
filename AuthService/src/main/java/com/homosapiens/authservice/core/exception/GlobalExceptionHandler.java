package com.homosapiens.authservice.core.exception;


import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import com.homosapiens.authservice.core.locale.LanguageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex, HttpServletRequest request) {
        String lang = LanguageUtil.resolveLang(request);
        String message = LanguageUtil.translateMessage(ex.getMessage(), lang);
        return ResponseEntity
                .status(ex.getStatus())
                .body(new CustomResponseEntity(message, ex.getStatus().value(), null));
    }
}
