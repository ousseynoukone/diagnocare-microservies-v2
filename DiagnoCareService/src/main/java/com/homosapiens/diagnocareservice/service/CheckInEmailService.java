package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.CheckIn;

public interface CheckInEmailService {
    void sendCheckInReminder(CheckIn checkIn, boolean isSecondReminder);
}
