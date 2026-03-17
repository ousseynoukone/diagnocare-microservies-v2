package com.homosapiens.diagnocareservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homosapiens.diagnocareservice.core.kafka.KafkaProducer;
import com.homosapiens.diagnocareservice.model.entity.Symptom;
import com.homosapiens.diagnocareservice.service.SymptomService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class SymptomControllerIntegrationTest {

    @MockBean
    private KafkaProducer kafkaProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SymptomService symptomService;

    private Long existingSymptomId;

    @BeforeEach
    void setUp() {
        Symptom symptom = new Symptom();
        symptom.setLabel("Test Headache");
        symptom.setSymptomLabelId(1L);
        Symptom created = symptomService.createSymptom(symptom);
        existingSymptomId = created.getId();
    }

    @Test
    void deleteSymptom_ShouldReturnNoContent_WhenSymptomExists() throws Exception {
        mockMvc.perform(delete("/symptoms/" + existingSymptomId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSymptomById_ShouldReturnSymptom_WhenSymptomExists() throws Exception {
        mockMvc.perform(get("/symptoms/" + existingSymptomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(existingSymptomId))
                .andExpect(jsonPath("$.data.label").value("Test Headache"));
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
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void searchSymptomsByLabel_ShouldReturnMatchingSymptoms() throws Exception {
        mockMvc.perform(get("/symptoms/search").param("label", "head"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
