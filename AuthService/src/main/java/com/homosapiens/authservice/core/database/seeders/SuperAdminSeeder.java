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
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLookupService userLookupService;

    @Value("${app.superadmin.email:superadmin@diagnocare.com}")
    private String email;

    @Value("${app.superadmin.password:SuperAdmin@diagnocare1}")
    private String password;

    @Value("${app.superadmin.first-name:Super}")
    private String firstName;

    @Value("${app.superadmin.last-name:Admin}")
    private String lastName;

    @Override
    @Transactional
    public void run(String... args) {
        Role superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (superAdminRole == null) {
            log.warn("SuperAdminSeeder: SUPER_ADMIN role not found — make sure RoleSeeder ran first.");
            return;
        }

        if (userLookupService.existsByEmail(email)) {
            log.info("SuperAdminSeeder: Super admin already exists, skipping.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setEmail(email);
        superAdmin.setFirstName(firstName);
        superAdmin.setLastName(lastName);
        superAdmin.setPassword(passwordEncoder.encode(password));
        superAdmin.setEmailVerified(true);
        superAdmin.setPrivacyPolicyAccepted(true);
        superAdmin.setTermsAccepted(true);
        superAdmin.setConsentDate(new Date());
        superAdmin.setConsentVersion("v1.0-2026");
        superAdmin.setLang("fr");
        superAdmin.setRoles(List.of(superAdminRole));

        userRepository.save(superAdmin);
        log.info("SuperAdminSeeder: Super admin created → {}", email);
    }
}
