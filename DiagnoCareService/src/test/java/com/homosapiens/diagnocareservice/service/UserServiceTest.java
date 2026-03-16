package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import com.homosapiens.diagnocareservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldReturnSavedUser_WhenUserIsValid() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() {
        Long userId = 1L;
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Smith");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, user);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Jane", result.getFirstName());
        verify(userRepository).existsById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowRuntimeException_WhenUserDoesNotExist() {
        Long userId = 999L;
        User user = new User();
        user.setFirstName("Jane");

        when(userRepository.existsById(userId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.updateUser(userId, user));

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        Long userId = 1L;

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        String email = "missing@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail(email);

        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithRole() {
        RoleEnum role = RoleEnum.ADMIN;
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("doctor1@example.com");

        List<User> users = Arrays.asList(user1);

        when(userRepository.findByRoles_Name(role)).thenReturn(users);

        List<User> result = userService.getUsersByRole(role);

        assertEquals(1, result.size());
        verify(userRepository).findByRoles_Name(role);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        String email = "test@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "missing@example.com";

        when(userRepository.existsByEmail(email)).thenReturn(false);

        boolean result = userService.existsByEmail(email);

        assertFalse(result);
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void getUserByPhoneNumber_ShouldReturnUser_WhenUserExists() {
        String phoneNumber = "+1234567890";
        User user = new User();
        user.setId(1L);
        user.setPhoneNumber(phoneNumber);

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByPhoneNumber(phoneNumber);

        assertTrue(result.isPresent());
        assertEquals(phoneNumber, result.get().getPhoneNumber());
        verify(userRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void getUserByPhoneNumber_ShouldReturnEmpty_WhenUserDoesNotExist() {
        String phoneNumber = "+9999999999";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByPhoneNumber(phoneNumber);

        assertFalse(result.isPresent());
        verify(userRepository).findByPhoneNumber(phoneNumber);
    }
}
