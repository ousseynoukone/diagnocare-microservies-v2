package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.SymptomDTO;
import com.homosapiens.diagnocareservice.model.entity.Symptom;
import com.homosapiens.diagnocareservice.repository.SymptomRepository;
import com.homosapiens.diagnocareservice.service.impl.SymptomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomServiceTest {

    @Mock
    private SymptomRepository symptomRepository;

    @InjectMocks
    private SymptomServiceImpl symptomService;

    @Test
    void createSymptom_ShouldReturnSavedSymptom_WhenSymptomIsValid() {
        Symptom symptom = new Symptom();
        symptom.setLabel("Headache");
        symptom.setSymptomLabelId(1L);

        Symptom savedSymptom = new Symptom();
        savedSymptom.setId(1L);
        savedSymptom.setLabel("Headache");
        savedSymptom.setSymptomLabelId(1L);

        when(symptomRepository.save(any(Symptom.class))).thenReturn(savedSymptom);

        Symptom result = symptomService.createSymptom(symptom);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Headache", result.getLabel());
        verify(symptomRepository).save(symptom);
    }

    @Test
    void updateSymptom_ShouldReturnUpdatedSymptom_WhenSymptomExists() {
        Long symptomId = 1L;
        Symptom symptom = new Symptom();
        symptom.setLabel("Updated Headache");
        symptom.setSymptomLabelId(1L);

        Symptom updatedSymptom = new Symptom();
        updatedSymptom.setId(symptomId);
        updatedSymptom.setLabel("Updated Headache");
        updatedSymptom.setSymptomLabelId(1L);

        when(symptomRepository.existsById(symptomId)).thenReturn(true);
        when(symptomRepository.save(any(Symptom.class))).thenReturn(updatedSymptom);

        Symptom result = symptomService.updateSymptom(symptomId, symptom);

        assertNotNull(result);
        assertEquals(symptomId, result.getId());
        assertEquals("Updated Headache", result.getLabel());
        verify(symptomRepository).existsById(symptomId);
        verify(symptomRepository).save(any(Symptom.class));
    }

    @Test
    void updateSymptom_ShouldThrowRuntimeException_WhenSymptomDoesNotExist() {
        Long symptomId = 999L;
        Symptom symptom = new Symptom();
        symptom.setLabel("Updated Headache");

        when(symptomRepository.existsById(symptomId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> symptomService.updateSymptom(symptomId, symptom));

        assertEquals("Symptom not found with id: " + symptomId, exception.getMessage());
        verify(symptomRepository).existsById(symptomId);
        verify(symptomRepository, never()).save(any(Symptom.class));
    }

    @Test
    void deleteSymptom_ShouldDeleteSymptom_WhenSymptomExists() {
        Long symptomId = 1L;

        symptomService.deleteSymptom(symptomId);

        verify(symptomRepository).deleteById(symptomId);
    }

    @Test
    void getSymptomById_ShouldReturnSymptom_WhenSymptomExists() {
        Long symptomId = 1L;
        Symptom symptom = new Symptom();
        symptom.setId(symptomId);
        symptom.setLabel("Headache");

        when(symptomRepository.findById(symptomId)).thenReturn(Optional.of(symptom));

        Optional<Symptom> result = symptomService.getSymptomById(symptomId);

        assertTrue(result.isPresent());
        assertEquals(symptomId, result.get().getId());
        verify(symptomRepository).findById(symptomId);
    }

    @Test
    void getSymptomById_ShouldReturnEmpty_WhenSymptomDoesNotExist() {
        Long symptomId = 999L;

        when(symptomRepository.findById(symptomId)).thenReturn(Optional.empty());

        Optional<Symptom> result = symptomService.getSymptomById(symptomId);

        assertFalse(result.isPresent());
        verify(symptomRepository).findById(symptomId);
    }

    @Test
    void getAllSymptoms_ShouldReturnAllSymptoms() {
        Symptom symptom1 = new Symptom();
        symptom1.setId(1L);
        symptom1.setLabel("Headache");

        Symptom symptom2 = new Symptom();
        symptom2.setId(2L);
        symptom2.setLabel("Fever");

        List<Symptom> symptoms = Arrays.asList(symptom1, symptom2);

        when(symptomRepository.findAll()).thenReturn(symptoms);

        List<Symptom> result = symptomService.getAllSymptoms();

        assertEquals(2, result.size());
        verify(symptomRepository).findAll();
    }

    @Test
    void searchSymptomsByLabel_ShouldReturnMatchingSymptoms() {
        String searchTerm = "head";
        Symptom symptom1 = new Symptom();
        symptom1.setId(1L);
        symptom1.setLabel("Headache");

        List<Symptom> symptoms = Arrays.asList(symptom1);

        when(symptomRepository.findByLabelContainingIgnoreCase(searchTerm)).thenReturn(symptoms);

        List<Symptom> result = symptomService.searchSymptomsByLabel(searchTerm);

        assertEquals(1, result.size());
        assertEquals("Headache", result.get(0).getLabel());
        verify(symptomRepository).findByLabelContainingIgnoreCase(searchTerm);
    }

    @Test
    void convertToDTO_ShouldConvertSymptomToDTO() {
        Symptom symptom = new Symptom();
        symptom.setId(1L);
        symptom.setLabel("Headache");
        symptom.setSymptomLabelId(1L);

        SymptomDTO dto = symptomService.convertToDTO(symptom);

        assertEquals(1L, dto.getId());
        assertEquals("Headache", dto.getLabel());
        assertEquals(1L, dto.getSymptomLabelId());
    }

    @Test
    void convertToDTOList_ShouldConvertListOfSymptomsToDTOs() {
        Symptom symptom1 = new Symptom();
        symptom1.setId(1L);
        symptom1.setLabel("Headache");

        Symptom symptom2 = new Symptom();
        symptom2.setId(2L);
        symptom2.setLabel("Fever");

        List<Symptom> symptoms = Arrays.asList(symptom1, symptom2);

        List<SymptomDTO> dtos = symptomService.convertToDTOList(symptoms);

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }
}
