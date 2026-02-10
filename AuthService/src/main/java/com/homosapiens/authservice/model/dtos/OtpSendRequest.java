package com.homosapiens.authservice.model.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpSendRequest {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;
}
