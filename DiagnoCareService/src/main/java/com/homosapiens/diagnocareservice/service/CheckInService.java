package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.CheckInCreateRequestDTO;
import com.homosapiens.diagnocareservice.dto.CheckInResponseDTO;

import java.util.List;

public interface CheckInService {
    CheckInResponseDTO activateCheckIn(Long predictionId, Long userId);
    CheckInResponseDTO submitCheckIn(CheckInCreateRequestDTO requestDTO);
    List<CheckInResponseDTO> getCheckInsByUser(Long userId);
}
