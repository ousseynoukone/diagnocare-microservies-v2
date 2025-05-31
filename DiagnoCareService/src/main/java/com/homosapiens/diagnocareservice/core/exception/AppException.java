package com.homosapiens.diagnocareservice.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AppException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public AppException(Throwable cause) {
        super(cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

}