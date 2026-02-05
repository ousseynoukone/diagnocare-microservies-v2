package com.homosapiens.authservice.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be valid")
    private String phoneNumber;

    private String lang;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
