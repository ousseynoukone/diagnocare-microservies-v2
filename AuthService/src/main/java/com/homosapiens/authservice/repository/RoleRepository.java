package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoleRepository extends JpaRepository<Role, Long> {
   Role findByName (RoleEnum roleEnum);
}
