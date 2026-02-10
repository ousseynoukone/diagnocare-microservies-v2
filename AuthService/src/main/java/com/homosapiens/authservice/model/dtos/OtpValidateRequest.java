package com.homosapiens.authservice.model.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpValidateRequest {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "OTP code must not be blank")
    private String code;
}
