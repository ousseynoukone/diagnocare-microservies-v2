package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Role;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(RoleEnum name);
}
