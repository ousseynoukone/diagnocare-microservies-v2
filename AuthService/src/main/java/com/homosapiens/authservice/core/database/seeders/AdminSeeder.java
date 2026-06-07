package com.homosapiens.authservice.core.database.seeders;

import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.enums.RoleEnum;
import com.homosapiens.authservice.repository.RoleRepository;
import com.homosapiens.authservice.repository.UserRepository;
import com.homosapiens.authservice.service.UserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLookupService userLookupService;

    @Value("${app.admin.email:admin@diagnocare.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@diagnocare1}")
    private String adminPassword;

    @Value("${app.admin.first-name:Admin}")
    private String adminFirstName;

    @Value("${app.admin.last-name:DiagnoCare}")
    private String adminLastName;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN);
        if (adminRole == null) {
            log.warn("AdminSeeder: ADMIN role not found — make sure RoleSeeder ran first.");
            return;
        }

        if (userLookupService.existsByEmail(adminEmail)) {
            log.info("AdminSeeder: Default admin already exists, skipping.");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmailVerified(true);
        admin.setPrivacyPolicyAccepted(true);
        admin.setTermsAccepted(true);
        admin.setConsentDate(new Date());
        admin.setConsentVersion("v1.0-2024");
        admin.setLang("fr");
        admin.setRoles(List.of(adminRole));

        userRepository.save(admin);
        log.info("AdminSeeder: Default admin created → {}", adminEmail);
    }
}
