package com.homosapiens.authservice.model.dtos;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
