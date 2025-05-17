package com.homosapiens.authservice.controller;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.RefreshTokenRequest;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.service.AuthService;
import com.homosapiens.authservice.service.helpers.ValidationHelper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class AuthController {
    private final AuthService authService;

    @PostMapping("login")
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

    @PostMapping("refresh-token")
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
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Bearer token", required = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader) {

            // Extract the token from the Authorization header
            String token = authorizationHeader.substring("Bearer ".length()).trim();

            // Delegate token validation to userAuthProvider
            return ResponseEntity.ok().body(authService.validateToken(token));

    }

}
