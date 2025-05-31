package com.homosapiens.diagnocareservice.core.exception.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomResponseEntity {
    String message;
    Integer statusCode;
    Object data;
}
