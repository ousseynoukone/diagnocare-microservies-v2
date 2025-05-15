package com.homosapiens.authservice.core.exception.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor

public class CustomResponseEntity {
    String message;
    int statusCode;
}
