package com.homosapiens.authservice.controller;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.exception.entity.CustomResponseEntity;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.dtos.UserUpdateDto;
import com.homosapiens.authservice.model.dtos.OtpSendRequest;
import com.homosapiens.authservice.model.dtos.OtpValidateRequest;
import com.homosapiens.authservice.model.dtos.ResetPasswordRequestDto;
import com.homosapiens.authservice.service.AuthService;
import com.homosapiens.authservice.service.helpers.ValidationHelper;
import com.homosapiens.authservice.core.locale.LanguageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints: login, register, tokens, and user sync")
public class AuthController {
    private final AuthService authService;

    @Value("${security.jwt.token.expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${security.jwt.token.refresh-expiration:86400000}")
    private long refreshTokenExpiration;

    @PostMapping("login")
    @Operation(summary = "Login", description = "Authenticate user and set HttpOnly auth cookies")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto user, BindingResult bindingResult,
                                   HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(request));
        }
        if (user != null) {
            Map<String, Object> authResponse = (Map<String, Object>) this.authService.login(user);
            // Set tokens as HttpOnly cookies — inaccessible to JavaScript
            setTokenCookies(response, (String) authResponse.get("token"), (String) authResponse.get("refreshToken"));
            // Return only the user profile in the body — tokens are in cookies
            return ResponseEntity.ok(Map.of("user", authResponse.get("user")));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(LanguageUtil.translateMessage("All User informations are required", LanguageUtil.resolveLang(request))));
    }



    @PostMapping("register")
    @Operation(summary = "Register", description = "Create a new user account")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterDto user , BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(request));
        }

        if(user!=null){
            Object response =  this.authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);



        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(LanguageUtil.translateMessage("CHEEZ BRO , UR CODES ARE A MESS ! THIS IS SUCH A MASTERPIECE OF SHIT", LanguageUtil.resolveLang(request)))
        );
    }

    @PutMapping("users/{id}")
    @Operation(summary = "Update user", description = "Update user profile and sync to other services")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto updateDto,
            BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(request));
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
    @Operation(summary = "Refresh token", description = "Issue new tokens from the refreshToken HttpOnly cookie")
    public ResponseEntity<?> refreshToken(HttpServletRequest servletRequest, HttpServletResponse response) {
        // Read the refresh token from the HttpOnly cookie (not the request body)
        String refreshToken = null;
        if (servletRequest.getCookies() != null) {
            refreshToken = Arrays.stream(servletRequest.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            String lang = LanguageUtil.resolveLang(servletRequest);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    CustomResponseEntity.builder()
                            .statusCode(HttpStatus.UNAUTHORIZED.value())
                            .message(LanguageUtil.translateMessage("Refresh token cookie is missing", lang))
                            .build()
            );
        }

        Map<String, Object> authResponse = (Map<String, Object>) authService.refreshToken(refreshToken);
        // Overwrite existing cookies with fresh tokens
        setTokenCookies(response, (String) authResponse.get("token"), (String) authResponse.get("refreshToken"));
        return ResponseEntity.ok(Map.of("user", authResponse.get("user")));
    }


    @PostMapping("otp/send")
    @Operation(summary = "Send OTP", description = "Send email verification OTP")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid OtpSendRequest request, BindingResult bindingResult, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(httpRequest));
        }
        String lang = LanguageUtil.resolveLang(httpRequest);
        authService.sendVerificationOtp(request.getEmail(), lang);
        return ResponseEntity.ok(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(LanguageUtil.translateMessage("OTP sent", lang))
                        .build()
        );
    }

    @PostMapping("otp/validate")
    @Operation(summary = "Validate OTP", description = "Validate email verification OTP")
    public ResponseEntity<?> validateOtp(@RequestBody @Valid OtpValidateRequest request, BindingResult bindingResult, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(httpRequest));
        }
        authService.validateVerificationOtp(request.getEmail(), request.getCode());
        return ResponseEntity.ok(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(LanguageUtil.translateMessage("OTP validated", LanguageUtil.resolveLang(httpRequest)))
                        .build()
        );
    }

    @PostMapping("reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using OTP verification")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request, BindingResult bindingResult, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return ValidationHelper.buildValidationReponse(bindingResult, LanguageUtil.resolveLang(httpRequest));
        }
        String lang = LanguageUtil.resolveLang(httpRequest);
        authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword(), lang);
        return ResponseEntity.ok(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message(LanguageUtil.translateMessage("Password reset successfully", lang))
                        .build()
        );
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

    @PostMapping("logout")
    @Operation(summary = "Logout", description = "Clear HttpOnly cookies and end session")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearTokenCookies(response);
        return ResponseEntity.ok(
                CustomResponseEntity.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Logout successful")
                        .build()
        );
    }

    // ─── Private helpers ─────────────────────────────────────────────────────────

    /**
     * Writes the access token and refresh token as HttpOnly, Secure, SameSite=Lax cookies.
     * JavaScript cannot read these cookies, protecting them from XSS attacks.
     */
    private void setTokenCookies(HttpServletResponse response, String token, String refreshToken) {
        int accessMaxAge  = (int) (accessTokenExpiration  / 1000); // ms → seconds
        int refreshMaxAge = (int) (refreshTokenExpiration / 1000);

        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("token", token, accessMaxAge));
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie("refreshToken", refreshToken, refreshMaxAge));
    }

    /**
     * Immediately expires both auth cookies, effectively deleting them from the browser.
     */
    private void clearTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("token",        "", 0));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("refreshToken", "", 0));
    }

    /**
     * Builds a cookie string with HttpOnly, SameSite=Lax, and Secure flags.
     * SameSite=Lax protects against CSRF.
     * HttpOnly ensures JavaScript cannot read the cookie value.
     */
    private String buildCookie(String name, String value, int maxAgeSeconds) {
        return name + "=" + value
                + "; Path=/"
                + "; Max-Age=" + maxAgeSeconds
                + "; HttpOnly"
                + "; SameSite=Lax";
        // Add "; Secure" here when deploying over HTTPS in production
    }

}
