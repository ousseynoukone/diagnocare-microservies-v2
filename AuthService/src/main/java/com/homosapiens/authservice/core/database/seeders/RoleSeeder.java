package com.homosapiens.authservice.core.database.seeders;

import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.enums.RoleEnum;
import com.homosapiens.authservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleRepository.findByName(roleEnum)==null) {
                Role role = new Role();
                role.setName(roleEnum);
                role.setDescription("Default description for " + roleEnum.name());
                roleRepository.save(role);
                System.out.println("Inserted role: " + roleEnum);
            }
        }
    }
}
