package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.mapper.UserMapper;
import com.homosapiens.authservice.repository.RoleRepository;
import com.homosapiens.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service

public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTAuthProvider jwtAuthProvider;

    public Object login(UserLoginDto loginDto) {
        User u = userRepository.findUserByEmail(loginDto.getEmail());
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), u.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email or password incorrect");
        }

        CustomUserDetails customUserDetails = buildCustomUserDetails(u);
        return buildTokenResponse(customUserDetails, u);
    }

    public User register(UserRegisterDto user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Role not found"));

        User realUser = UserMapper.toEntity(user, role);
        return userRepository.save(realUser);
    }

    public Object validateToken(String token) {
        Authentication authentication = jwtAuthProvider.validateToken(token);
        return authentication.getPrincipal();
    }

    public Object refreshToken(String refreshToken) {
        Authentication authentication = jwtAuthProvider.validateRefreshToken(refreshToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getPrincipal();
        String email = (String) details.get("email");


        User u = userRepository.findUserByEmail(email);
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }

        CustomUserDetails customUserDetails = buildCustomUserDetails(u);
        return buildTokenResponse(customUserDetails, u);
    }

    // -------------------- Private Helpers --------------------

    private CustomUserDetails buildCustomUserDetails(User u) {
        return CustomUserDetails.builder()
                .email(u.getEmail())
                .id(u.getId())
                .roles(u.getRoles())
                .build();
    }

    private Map<String, Object> buildTokenResponse(CustomUserDetails userDetails, User user) {
        Map<String, Object> tokenResponse = jwtAuthProvider.createToken(userDetails);
        Map<String, Object> refreshTokenResponse = jwtAuthProvider.createRefreshToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", tokenResponse.get("token"));
        response.put("tokenValidity", tokenResponse.get("validity"));
        response.put("refreshToken", refreshTokenResponse.get("refreshToken"));
        response.put("refreshTokenValidity", refreshTokenResponse.get("validity"));
        response.put("user", user);

        return response;
    }
}
