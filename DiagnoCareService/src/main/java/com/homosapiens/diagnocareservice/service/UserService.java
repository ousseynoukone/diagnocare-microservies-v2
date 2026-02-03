package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(RoleEnum role);
    boolean existsByEmail(String email);
    Optional<User> getUserByPhoneNumber(String phoneNumber);
}