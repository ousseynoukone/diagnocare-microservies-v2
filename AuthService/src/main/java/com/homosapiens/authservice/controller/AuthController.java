package com.homosapiens.authservice.controller;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.RefreshTokenRequest;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.dtos.UserUpdateDto;
import com.homosapiens.authservice.service.AuthService;
import com.homosapiens.authservice.service.helpers.ValidationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints: login, register, tokens, and user sync")
public class AuthController {
    private final AuthService authService;

    @PostMapping("login")
    @Operation(summary = "Login", description = "Authenticate user and return access + refresh tokens")
    private ResponseEntity<?> login(@RequestBody @Valid UserLoginDto user , BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult);
        }
        if(user!=null){
                Object response =  this.authService.login(user);
                return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message("All User informations are required"));
    }



    @PostMapping("register")
    @Operation(summary = "Register", description = "Create a new user account")
    private ResponseEntity<?> register(@RequestBody @Valid UserRegisterDto user , BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult);
        }

        if(user!=null){
            Object response =  this.authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);



        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("CHEEZ BRO , UR CODES ARE A MESS ! THIS IS SUCH A MASTERPIECE OF SHIT")
        );
    }

    @PutMapping("users/{id}")
    @Operation(summary = "Update user", description = "Update user profile and sync to other services")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto updateDto,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult);
        }
        return ResponseEntity.ok(authService.updateUser(id, updateDto));
    }

    @DeleteMapping("users/{id}")
    @Operation(summary = "Delete user", description = "Delete user account and sync deletion")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("refresh-token")
    @Operation(summary = "Refresh token", description = "Issue a new access token using a refresh token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CustomResponseEntity.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .message("Refresh token is required")
                            .build()
            );
        }

        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }



    @PostMapping("validate-token")
    @Operation(summary = "Validate token", description = "Validate access token and return user details")
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader) {

            // Extract the token from the Authorization header
            String token = authorizationHeader.substring("Bearer ".length()).trim();

            // Delegate token validation to userAuthProvider
            return ResponseEntity.ok().body(authService.validateToken(token));

    }

}
