package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.Otp;
import com.homosapiens.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    List<Otp> findByUserAndUsedAtIsNull(User user);

    Optional<Otp> findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);

    long deleteByExpiresAtBefore(Date date);
}
