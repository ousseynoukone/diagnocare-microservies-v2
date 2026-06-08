package com.homosapiens.diagnocareservice.config;

import com.homosapiens.diagnocareservice.model.entity.Role;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import com.homosapiens.diagnocareservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleRepository.findByName(roleEnum) == null) {
                Role role = new Role();
                role.setName(roleEnum);
                role.setDescription("Default description for " + roleEnum.name());
                roleRepository.save(role);
                log.info("RoleSeeder: inserted role {}", roleEnum.name());
            }
        }
    }
}
