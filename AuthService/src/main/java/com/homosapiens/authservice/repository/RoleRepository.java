package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/roles")
public interface RoleRepository extends JpaRepository<Role, Long> {
}
