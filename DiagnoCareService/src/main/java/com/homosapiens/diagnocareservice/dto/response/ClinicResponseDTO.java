package com.homosapiens.diagnocareservice.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClinicResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private UserDTO user;

    @Data
    public static class UserDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
    }
} 