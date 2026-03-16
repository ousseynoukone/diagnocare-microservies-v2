package com.homosapiens.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


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

        mockMvc.perform( post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content(objectMapper.writeValueAsString(payload))
                .accept(MediaType.APPLICATION_JSON)

        ).andExpect(status().is2xxSuccessful())
                // Response wrapper returns data.email (not root email)
                .andExpect(jsonPath("$.data.email").value("john.arc.raider@gmail.com"));

    }
}

