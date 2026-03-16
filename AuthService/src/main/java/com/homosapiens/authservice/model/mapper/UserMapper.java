package com.homosapiens.authservice.model.mapper;


import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;

import java.util.Collections;

public class UserMapper {

    public static User toEntity(UserRegisterDto dto, Role role) {
        User user = new User();

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(dto.getPassword());
        user.setLang(dto.getLang());

        if (role != null) {
            user.setRoles(Collections.singletonList(role));
        }

        return user;
    }
}
