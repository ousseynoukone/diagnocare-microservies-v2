package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByEmail(String email);
}
