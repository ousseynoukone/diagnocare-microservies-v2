package com.diagnocare.gateway.data.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String token;
    private String role;
    private String lang;

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", role='" + role + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }
}
