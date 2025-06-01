package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository  extends JpaRepository<Availability, Long> {


}
