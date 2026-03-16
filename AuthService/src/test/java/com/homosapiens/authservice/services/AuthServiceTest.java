package com.homosapiens.authservice.services;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.dtos.UserUpdateDto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    void login_ShouldThrowUnauthorized_WhenPasswordIsIncorrect() {
        // Arrange
        UserLoginDto loginDto = UserLoginDto.builder()
                .email("john.doe@example.com")
                .password("wrong-password")
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setPassword("encoded-password");
        user.setEmailVerified(true);

        when(userRepository.findUserByEmail("john.doe@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.login(loginDto));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Email or password incorrect", exception.getMessage());
    }

    @Test
    void login_ShouldThrowUnauthorized_WhenEmailNotVerified() {
        // Arrange
        UserLoginDto loginDto = UserLoginDto.builder()
                .email("john.doe@example.com")
                .password("plain-password")
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setPassword("encoded-password");
        user.setEmailVerified(false);

        when(userRepository.findUserByEmail("john.doe@example.com")).thenReturn(user);
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.login(loginDto));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Email not verified", exception.getMessage());
    }

    @Test
    void register_ShouldReturnUser_WhenRegistrationIsSuccessful() {
        // Arrange
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setEmail("newuser@example.com");
        registerDto.setFirstName("John");
        registerDto.setLastName("Doe");
        registerDto.setPassword("password123");
        registerDto.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setName("PATIENT");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setEmailVerified(false);

        when(userRepository.findUserByEmail("newuser@example.com")).thenReturn(null);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = authService.register(registerDto);

        // Assert
        assertEquals(savedUser, result);
        assertEquals("newuser@example.com", result.getEmail());
    }

    @Test
    void register_ShouldThrowConflict_WhenEmailAlreadyExists() {
        // Arrange
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setEmail("existing@example.com");
        registerDto.setFirstName("John");
        registerDto.setLastName("Doe");
        registerDto.setPassword("password123");
        registerDto.setRoleId(1L);

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findUserByEmail("existing@example.com")).thenReturn(existingUser);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.register(registerDto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void register_ShouldThrowNotFound_WhenRoleDoesNotExist() {
        // Arrange
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setEmail("newuser@example.com");
        registerDto.setFirstName("John");
        registerDto.setLastName("Doe");
        registerDto.setPassword("password123");
        registerDto.setRoleId(999L);

        when(userRepository.findUserByEmail("newuser@example.com")).thenReturn(null);
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.register(registerDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUpdateIsSuccessful() {
        // Arrange
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setPhoneNumber("+1234567890");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("user@example.com");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("user@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPhoneNumber("+1234567890");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = authService.updateUser(userId, updateDto);

        // Assert
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("+1234567890", result.getPhoneNumber());
    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFirstName("Jane");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.updateUser(userId, updateDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateUser_ShouldThrowConflict_WhenNewEmailAlreadyExists() {
        // Arrange
        Long userId = 1L;
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("taken@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("user@example.com");

        User userWithTakenEmail = new User();
        userWithTakenEmail.setId(2L);
        userWithTakenEmail.setEmail("taken@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findUserByEmail("taken@example.com")).thenReturn(userWithTakenEmail);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.updateUser(userId, updateDto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        authService.deleteUser(userId);

        // Assert
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.deleteUser(userId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_ShouldReturnTokenResponse_WhenRefreshTokenIsValid() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String email = "user@example.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setLang("en");
        user.setRoles(Collections.emptyList());

        Map<String, Object> tokenDetails = new HashMap<>();
        tokenDetails.put("email", email);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(tokenDetails);

        Map<String, Object> accessToken = new HashMap<>();
        accessToken.put("token", "new-access-token");
        accessToken.put("validity", 3600L);

        Map<String, Object> refreshTokenResponse = new HashMap<>();
        refreshTokenResponse.put("refreshToken", "new-refresh-token");
        refreshTokenResponse.put("validity", 7200L);

        when(jwtAuthProvider.validateRefreshToken(refreshToken)).thenReturn(authentication);
        when(userRepository.findUserByEmail(email)).thenReturn(user);
        when(jwtAuthProvider.createToken(any(CustomUserDetails.class))).thenReturn(accessToken);
        when(jwtAuthProvider.createRefreshToken(any(CustomUserDetails.class))).thenReturn(refreshTokenResponse);

        // Act
        Object result = authService.refreshToken(refreshToken);

        // Assert
        Map<?, ?> response = assertInstanceOf(Map.class, result);
        assertEquals("new-access-token", response.get("token"));
        assertEquals("new-refresh-token", response.get("refreshToken"));
    }

    @Test
    void refreshToken_ShouldThrowNotFound_WhenUserDoesNotExist() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        String email = "missing@example.com";

        Map<String, Object> tokenDetails = new HashMap<>();
        tokenDetails.put("email", email);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(tokenDetails);

        when(jwtAuthProvider.validateRefreshToken(refreshToken)).thenReturn(authentication);
        when(userRepository.findUserByEmail(email)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> authService.refreshToken(refreshToken));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("User not found", exception.getMessage());
    }
}
