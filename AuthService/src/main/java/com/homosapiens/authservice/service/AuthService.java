package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTAuthProvider jwtAuthProvider;

    private Object login(UserLoginDto loginDto) {
        User u = userRepository.findUserByEmail(loginDto.getEmail());
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND,"User not found");
        }

        if (!passwordEncoder.matches(u.getPassword(), loginDto.getPassword())) {
            throw new AppException( HttpStatus.UNAUTHORIZED,"Email or password incorrect" );
        }

        CustomUserDetails customUserDetails = CustomUserDetails.builder()
                .email(u.getEmail())
                .id(u.getId())
                .roles(u.getRoles())
                .build();

        Map<String, Object> tokenResponse = jwtAuthProvider.createToken(customUserDetails);
        Map<String, Object> refreshTokenResponse = jwtAuthProvider.createRefreshToken(customUserDetails);

        // Extract token and validity from the responses
        String token = (String) tokenResponse.get("token");
        Date tokenValidity = (Date) tokenResponse.get("validity");

        String refreshToken = (String) refreshTokenResponse.get("refreshToken");
        Date refreshTokenValidity = (Date) refreshTokenResponse.get("validity");

        // Create the final response map
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);  // The access token
        response.put("tokenValidity", tokenValidity);  // The validity (expiration) of the access token
        response.put("refreshToken", refreshToken);  // The refresh token
        response.put("refreshTokenValidity", refreshTokenValidity);  // The validity (expiration) of the refresh token

        response.put("user", u);

        return response;
    }


}
