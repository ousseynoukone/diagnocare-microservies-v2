package com.homosapiens.diagnocareservice.service.impl.appointment.helper;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.repository.AppointmentRepository;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.impl.appointment.ScheduleSlotServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
public class AvailabilityHelper {
    private final ScheduleSlotRepository scheduleSlotRepository;

    public  boolean checkAvailability(Availability availability, Availability lastAvailability) {

        if (availability == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "availability is null");
        }

        if (isThereAnyBookedSlots(lastAvailability)) {
            throw new AppException(HttpStatus.NOT_ACCEPTABLE, "This your availability contains booked slots");
        }
        return true;
    }

    private boolean isThereAnyBookedSlots(Availability availability) {
     List<ScheduleSlot>  scheduleSlots =  scheduleSlotRepository.getScheduleSlotByAvailabilityId(availability.getId());
        return  scheduleSlots.stream().anyMatch(ScheduleSlot::isIsBooked);
    }

}
