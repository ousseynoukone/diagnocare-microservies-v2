package com.homosapiens.diagnocareservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homosapiens.diagnocareservice.dto.UserDataExportDTO;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.service.UserDataExportService;
import com.homosapiens.diagnocareservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;
    private final UserDataExportService userDataExportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserController(UserService userService, UserDataExportService userDataExportService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.userDataExportService = userDataExportService;
        this.objectMapper = objectMapper;
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> exportUserData(@PathVariable Long id) {
        try {
            UserDataExportDTO exportData = userDataExportService.exportUserData(id);
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "user-data-export-" + id + ".json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

} 