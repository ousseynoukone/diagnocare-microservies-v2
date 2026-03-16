package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.CheckInCreateRequestDTO;
import com.homosapiens.diagnocareservice.dto.CheckInResponseDTO;
import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.Prediction;

import java.util.List;

public interface CheckInService {
    CheckIn scheduleCheckIn(Prediction prediction);
    CheckInResponseDTO submitCheckIn(CheckInCreateRequestDTO requestDTO);
    List<CheckInResponseDTO> getCheckInsByUser(Long userId);
}
