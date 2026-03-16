package com.homosapiens.authservice.services;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.repository.RoleRepository;
import com.homosapiens.authservice.repository.UserRepository;
import com.homosapiens.authservice.service.AuthService;
import com.homosapiens.authservice.service.OtpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JWTAuthProvider jwtAuthProvider;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_ShouldReturnTokenResponse_WhenCredentialsAreValid() {
        // Arrange
        UserLoginDto loginDto = UserLoginDto.builder()
                .email("john.doe@example.com")
                .password("plain-password")
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setPassword("encoded-password");
        user.setEmailVerified(true);
        user.setLang("en");
        user.setRoles(Collections.emptyList());

        Map<String, Object> accessToken = new HashMap<>();
        accessToken.put("token", "access-token");
        accessToken.put("validity", 3600L);

        Map<String, Object> refreshToken = new HashMap<>();
        refreshToken.put("refreshToken", "refresh-token");
        refreshToken.put("validity", 7200L);

        when(userRepository.findUserByEmail("john.doe@example.com")).thenReturn(user);
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtAuthProvider.createToken(any(CustomUserDetails.class))).thenReturn(accessToken);
        when(jwtAuthProvider.createRefreshToken(any(CustomUserDetails.class))).thenReturn(refreshToken);

        // Act
        Object result = authService.login(loginDto);

        // Assert
        Map<?, ?> response = assertInstanceOf(Map.class, result);
        assertEquals("access-token", response.get("token"));
        assertEquals("refresh-token", response.get("refreshToken"));
        assertEquals(user, response.get("user"));
    }

    @Test
    void login_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        UserLoginDto loginDto = UserLoginDto.builder()
                .email("missing@example.com")
                .password("any-password")
                .build();

        when(userRepository.findUserByEmail("missing@example.com")).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.login(loginDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }
}
