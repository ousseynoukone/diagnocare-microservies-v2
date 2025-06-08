package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.Role;
import com.homosapiens.diagnocareservice.model.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);
    boolean existsByEmail(String email);
    Optional<User> getUserByPhoneNumber(String phoneNumber);
    Optional<User> getUserByStripeCustomerId(String stripeCustomerId);
    Optional<User> getUserByNpi(String npi);
} 