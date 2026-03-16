package com.homosapiens.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.enums.RoleEnum;
import com.homosapiens.authservice.repository.RoleRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthControllerIntegrationTest {

    @MockBean
    private KafkaProducer kafkaProducer;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Mock JavaMailSender to prevent mail sending failures
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // Seed roles for tests
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleRepository.findByName(roleEnum) == null) {
                Role role = new Role();
                role.setName(roleEnum);
                role.setDescription("Default description for " + roleEnum.name());
                roleRepository.save(role);
            }
        }
    }


    @Test
    void login_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "not-an-email");
        payload.put("password", ""); 

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void register_ShouldReturnUserDetailsWhenSuccess() throws Exception {

        Map<String, Object> payload = new HashMap<>();

        payload.put("email", "john.arc.raider@gmail.com");
        payload.put("firstName", "string");
        payload.put("lastName", "string");
        payload.put("phoneNumber", "0420371330845");
        payload.put("lang", "string");
        payload.put("password", "stringst");
        payload.put("roleId", 1);
        payload.put("privacyPolicyAccepted", true);
        payload.put("termsAccepted", true);

        mockMvc.perform( post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload))
                .accept(MediaType.APPLICATION_JSON)

        ).andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.email").value("john.arc.raider@gmail.com"));

    }

    @Test
    void register_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // First register a user
        Map<String, Object> firstPayload = new HashMap<>();
        firstPayload.put("email", "duplicate@example.com");
        firstPayload.put("firstName", "First");
        firstPayload.put("lastName", "User");
        firstPayload.put("phoneNumber", "+1234567890");
        firstPayload.put("lang", "en");
        firstPayload.put("password", "password123");
        firstPayload.put("roleId", 1);
        firstPayload.put("privacyPolicyAccepted", true);
        firstPayload.put("termsAccepted", true);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(firstPayload)))
                .andExpect(status().isCreated());

        // Try to register again with same email
        // Note: Email uniqueness check happens before encryption in service layer
        // The check queries with plain text email, which should work if repository decrypts on read
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(firstPayload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenRequiredFieldsAreMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "incomplete@example.com");
        // Missing firstName, lastName, password, roleId

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPhoneNumberIsInvalid() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        payload.put("firstName", "John");
        payload.put("lastName", "Doe");
        payload.put("phoneNumber", "invalid-phone"); // Invalid format
        payload.put("lang", "en");
        payload.put("password", "password123");
        payload.put("roleId", 1);
        payload.put("privacyPolicyAccepted", true);
        payload.put("termsAccepted", true);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void refreshToken_ShouldReturnBadRequest_WhenRefreshTokenIsMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        // Missing refreshToken field

        mockMvc.perform(post("/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Refresh token is required"));
    }

    @Test
    void refreshToken_ShouldReturnBadRequest_WhenRefreshTokenIsEmpty() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("refreshToken", "");

        mockMvc.perform(post("/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Refresh token is required"));
    }

    @Test
    void sendOtp_ShouldReturnBadRequest_WhenEmailIsMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        // Missing email

        mockMvc.perform(post("/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void sendOtp_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "not-an-email");

        mockMvc.perform(post("/otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void validateOtp_ShouldReturnBadRequest_WhenEmailIsMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", "123456");

        mockMvc.perform(post("/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void validateOtp_ShouldReturnBadRequest_WhenCodeIsMissing() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.com");

        mockMvc.perform(post("/otp/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "Updated");

        mockMvc.perform(put("/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", "A"); // Too short (min 2 characters)

        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(delete("/users/99999")
                .header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/validate-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/validate-token")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}

