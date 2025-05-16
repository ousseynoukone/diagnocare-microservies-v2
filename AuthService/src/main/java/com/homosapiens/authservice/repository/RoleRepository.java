package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface RoleRepository extends JpaRepository<Role, Long> {
   Role findByName (RoleEnum roleEnum);
}
