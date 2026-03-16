package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.PredictionDTO;
import com.homosapiens.diagnocareservice.dto.PredictionRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.repository.SessionSymptomRepository;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
import com.homosapiens.diagnocareservice.service.impl.PredictionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private SessionSymptomRepository sessionSymptomRepository;

    @Mock
    private PathologyResultService pathologyResultService;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    @Test
    void createPrediction_ShouldReturnSavedPrediction_WhenRequestIsValid() {
        PredictionRequestDTO requestDTO = new PredictionRequestDTO();
        requestDTO.setSessionSymptomId(1L);
        requestDTO.setBestScore(new BigDecimal("0.85"));
        requestDTO.setIsRedAlert(false);
        requestDTO.setComment("Test prediction");

        SessionSymptom sessionSymptom = new SessionSymptom();
        sessionSymptom.setId(1L);
        User user = new User();
        user.setId(1L);
        sessionSymptom.setUser(user);

        Prediction savedPrediction = new Prediction();
        savedPrediction.setId(1L);
        savedPrediction.setSessionSymptom(sessionSymptom);
        savedPrediction.setBestScore(new BigDecimal("0.85"));
        savedPrediction.setIsRedAlert(false);

        when(sessionSymptomRepository.findById(1L)).thenReturn(Optional.of(sessionSymptom));
        when(predictionRepository.save(any(Prediction.class))).thenReturn(savedPrediction);

        Prediction result = predictionService.createPrediction(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(sessionSymptomRepository).findById(1L);
        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    void createPrediction_ShouldThrowAppException_WhenSessionSymptomNotFound() {
        PredictionRequestDTO requestDTO = new PredictionRequestDTO();
        requestDTO.setSessionSymptomId(999L);

        when(sessionSymptomRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, 
            () -> predictionService.createPrediction(requestDTO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Session symptom not found"));
        verify(sessionSymptomRepository).findById(999L);
        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    void updatePrediction_ShouldReturnUpdatedPrediction_WhenPredictionExists() {
        Long predictionId = 1L;
        PredictionRequestDTO requestDTO = new PredictionRequestDTO();
        requestDTO.setBestScore(new BigDecimal("0.90"));
        requestDTO.setIsRedAlert(true);
        requestDTO.setComment("Updated comment");

        Prediction existingPrediction = new Prediction();
        existingPrediction.setId(predictionId);
        existingPrediction.setBestScore(new BigDecimal("0.85"));
        existingPrediction.setIsRedAlert(false);

        Prediction updatedPrediction = new Prediction();
        updatedPrediction.setId(predictionId);
        updatedPrediction.setBestScore(new BigDecimal("0.90"));
        updatedPrediction.setIsRedAlert(true);
        updatedPrediction.setComment("Updated comment");

        when(predictionRepository.findById(predictionId)).thenReturn(Optional.of(existingPrediction));
        when(predictionRepository.save(any(Prediction.class))).thenReturn(updatedPrediction);

        Prediction result = predictionService.updatePrediction(predictionId, requestDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.90"), result.getBestScore());
        assertTrue(result.getIsRedAlert());
        verify(predictionRepository).findById(predictionId);
        verify(predictionRepository).save(any(Prediction.class));
    }

    @Test
    void updatePrediction_ShouldThrowAppException_WhenPredictionNotFound() {
        Long predictionId = 999L;
        PredictionRequestDTO requestDTO = new PredictionRequestDTO();

        when(predictionRepository.findById(predictionId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, 
            () -> predictionService.updatePrediction(predictionId, requestDTO));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Prediction not found"));
        verify(predictionRepository).findById(predictionId);
        verify(predictionRepository, never()).save(any(Prediction.class));
    }

    @Test
    void deletePrediction_ShouldDeletePrediction_WhenPredictionExists() {
        Long predictionId = 1L;

        predictionService.deletePrediction(predictionId);

        verify(predictionRepository).deleteById(predictionId);
    }

    @Test
    void getPredictionById_ShouldReturnPrediction_WhenPredictionExists() {
        Long predictionId = 1L;
        Prediction prediction = new Prediction();
        prediction.setId(predictionId);
        prediction.setBestScore(new BigDecimal("0.85"));

        when(predictionRepository.findById(predictionId)).thenReturn(Optional.of(prediction));

        Optional<Prediction> result = predictionService.getPredictionById(predictionId);

        assertTrue(result.isPresent());
        assertEquals(predictionId, result.get().getId());
        verify(predictionRepository).findById(predictionId);
    }

    @Test
    void getPredictionById_ShouldReturnEmpty_WhenPredictionDoesNotExist() {
        Long predictionId = 999L;

        when(predictionRepository.findById(predictionId)).thenReturn(Optional.empty());

        Optional<Prediction> result = predictionService.getPredictionById(predictionId);

        assertFalse(result.isPresent());
        verify(predictionRepository).findById(predictionId);
    }

    @Test
    void getAllPredictions_ShouldReturnAllPredictions() {
        Prediction prediction1 = new Prediction();
        prediction1.setId(1L);

        Prediction prediction2 = new Prediction();
        prediction2.setId(2L);

        List<Prediction> predictions = Arrays.asList(prediction1, prediction2);

        when(predictionRepository.findAll()).thenReturn(predictions);

        List<Prediction> result = predictionService.getAllPredictions();

        assertEquals(2, result.size());
        verify(predictionRepository).findAll();
    }

    @Test
    void getRedAlertPredictions_ShouldReturnRedAlertPredictions() {
        Prediction redAlertPrediction = new Prediction();
        redAlertPrediction.setId(1L);
        redAlertPrediction.setIsRedAlert(true);

        List<Prediction> predictions = Arrays.asList(redAlertPrediction);

        when(predictionRepository.findByIsRedAlert(true)).thenReturn(predictions);

        List<Prediction> result = predictionService.getRedAlertPredictions();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsRedAlert());
        verify(predictionRepository).findByIsRedAlert(true);
    }

    @Test
    void getPredictionsBySessionSymptomId_ShouldReturnPredictions() {
        Long sessionSymptomId = 1L;
        Prediction prediction = new Prediction();
        prediction.setId(1L);

        List<Prediction> predictions = Arrays.asList(prediction);

        when(predictionRepository.findBySessionSymptomId(sessionSymptomId)).thenReturn(predictions);

        List<Prediction> result = predictionService.getPredictionsBySessionSymptomId(sessionSymptomId);

        assertEquals(1, result.size());
        verify(predictionRepository).findBySessionSymptomId(sessionSymptomId);
    }

    @Test
    void getPredictionsByUserId_ShouldReturnPredictions() {
        Long userId = 1L;
        Prediction prediction = new Prediction();
        prediction.setId(1L);

        List<Prediction> predictions = Arrays.asList(prediction);

        when(predictionRepository.findBySessionSymptomUserId(userId)).thenReturn(predictions);

        List<Prediction> result = predictionService.getPredictionsByUserId(userId);

        assertEquals(1, result.size());
        verify(predictionRepository).findBySessionSymptomUserId(userId);
    }

    @Test
    void convertToDTO_ShouldConvertPredictionToDTO() {
        SessionSymptom sessionSymptom = new SessionSymptom();
        sessionSymptom.setId(1L);

        Prediction prediction = new Prediction();
        prediction.setId(1L);
        prediction.setBestScore(new BigDecimal("0.85"));
        prediction.setIsRedAlert(false);
        prediction.setComment("Test comment");
        prediction.setSessionSymptom(sessionSymptom);

        PredictionDTO dto = predictionService.convertToDTO(prediction);

        assertEquals(1L, dto.getId());
        assertEquals(new BigDecimal("0.85"), dto.getBestScore());
        assertFalse(dto.getIsRedAlert());
        assertEquals("Test comment", dto.getComment());
        assertEquals(1L, dto.getSessionSymptomId());
    }
}
