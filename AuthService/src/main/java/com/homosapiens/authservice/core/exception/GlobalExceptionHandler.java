package com.homosapiens.authservice.core.exception;


import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import com.homosapiens.authservice.core.locale.LanguageUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.transaction.TransactionSystemException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String lang = LanguageUtil.resolveLang(request);
        String message = LanguageUtil.translateMessage("Validation failed", lang);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponseEntity(message, HttpStatus.BAD_REQUEST.value(), ex.getBindingResult().getAllErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String lang = LanguageUtil.resolveLang(request);
        // Keep original interpolated message(s) so the client sees the real reason (e.g. phoneNumber invalid)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<?> handleTransactionSystemException(TransactionSystemException ex, HttpServletRequest request) {
        // Unwrap JPA validation exceptions so they don't show up as 500
        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(cve, request);
        }
        String lang = LanguageUtil.resolveLang(request);
        String message = LanguageUtil.translateMessage("Request processing failed", lang);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomResponseEntity(message, HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }
}
