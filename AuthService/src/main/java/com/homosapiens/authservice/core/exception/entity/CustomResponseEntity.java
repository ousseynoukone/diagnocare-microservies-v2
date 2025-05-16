package com.homosapiens.authservice.core.exception.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomResponseEntity {
    String message;
    Integer statusCode;
    Object data;
}
