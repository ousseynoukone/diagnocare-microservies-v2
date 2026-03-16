package com.homosapiens.diagnocareservice.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class SymptomControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSymptom_ShouldReturnCreatedSymptom_WhenSymptomIsValid() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("label", "Headache");
        payload.put("symptomLabelId", 1L);

        mockMvc.perform(post("/symptoms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Headache"))
                .andExpect(jsonPath("$.symptomLabelId").value(1));
    }

    @Test
    void updateSymptom_ShouldReturnUpdatedSymptom_WhenSymptomExists() throws Exception {
        // First create a symptom
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("label", "Original Headache");
        createPayload.put("symptomLabelId", 1L);

        String createResponse = mockMvc.perform(post("/symptoms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long symptomId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update the symptom
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("label", "Updated Headache");
        updatePayload.put("symptomLabelId", 1L);

        mockMvc.perform(put("/symptoms/" + symptomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Updated Headache"));
    }

    @Test
    void updateSymptom_ShouldReturnNotFound_WhenSymptomDoesNotExist() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("label", "Updated Headache");

        mockMvc.perform(put("/symptoms/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSymptom_ShouldReturnNoContent_WhenSymptomExists() throws Exception {
        // First create a symptom
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("label", "Temporary Headache");
        createPayload.put("symptomLabelId", 999L);

        String createResponse = mockMvc.perform(post("/symptoms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long symptomId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete the symptom
        mockMvc.perform(delete("/symptoms/" + symptomId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSymptomById_ShouldReturnSymptom_WhenSymptomExists() throws Exception {
        // First create a symptom
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("label", "Test Headache");
        createPayload.put("symptomLabelId", 2L);

        String createResponse = mockMvc.perform(post("/symptoms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long symptomId = objectMapper.readTree(createResponse).get("id").asLong();

        // Get the symptom
        mockMvc.perform(get("/symptoms/" + symptomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(symptomId))
                .andExpect(jsonPath("$.label").value("Test Headache"));
    }

    @Test
    void getSymptomById_ShouldReturnNotFound_WhenSymptomDoesNotExist() throws Exception {
        mockMvc.perform(get("/symptoms/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSymptoms_ShouldReturnListOfSymptoms() throws Exception {
        mockMvc.perform(get("/symptoms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchSymptomsByLabel_ShouldReturnMatchingSymptoms() throws Exception {
        // First create a symptom
        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("label", "Headache");
        createPayload.put("symptomLabelId", 3L);

        mockMvc.perform(post("/symptoms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated());

        // Search for symptoms
        mockMvc.perform(get("/symptoms/search")
                .param("label", "head"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
