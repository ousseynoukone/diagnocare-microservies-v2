package com.homosapiens.authservice.model.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailChangeRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "New email is required")
    @Email(message = "New email should be valid")
    private String newEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
