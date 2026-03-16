package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.kafka.KafkaProducer;
import com.homosapiens.authservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.dtos.UserSyncEventDTO;
import com.homosapiens.authservice.model.dtos.UserUpdateDto;
import com.homosapiens.authservice.model.mapper.UserMapper;
import com.homosapiens.authservice.repository.RoleRepository;
import com.homosapiens.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service

public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTAuthProvider jwtAuthProvider;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private OtpService otpService;

    public Object login(UserLoginDto loginDto) {
        User u = userRepository.findUserByEmail(loginDto.getEmail());
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (!passwordEncoder.matches(loginDto.getPassword(), u.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email or password incorrect");
        }
        if (!u.isEmailVerified()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Email not verified");
        }

        CustomUserDetails customUserDetails = buildCustomUserDetails(u);
        return buildTokenResponse(customUserDetails, u);
    }

    public User register(UserRegisterDto user) {
        User existingUser = userRepository.findUserByEmail(user.getEmail());
        if (existingUser != null) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Role not found"));

        User realUser = UserMapper.toEntity(user, role);
        realUser.setEmailVerified(false);
        User saved = userRepository.save(realUser);
        otpService.sendEmailVerificationOtp(saved, saved.getLang());
        sendUserEvent(KafkaEvent.USER_REGISTERED, saved, true);
        return saved;
    }

    public User updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        boolean emailChanged = false;
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            User existingUser = userRepository.findUserByEmail(updateDto.getEmail());
            if (existingUser != null) {
                throw new AppException(HttpStatus.CONFLICT, "Email already in use");
            }
            user.setEmail(updateDto.getEmail());
            user.setEmailVerified(false);
            emailChanged = true;
        }

        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getLang() != null) {
            user.setLang(updateDto.getLang());
        }
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        User saved = userRepository.save(user);
        if (emailChanged) {
            otpService.sendEmailVerificationOtp(saved, saved.getLang());
        }
        sendUserEvent(KafkaEvent.USER_UPDATE, saved, true);
        return saved;
    }

    public void sendVerificationOtp(String email, String lang) {
        otpService.sendEmailVerificationOtp(email, lang);
    }

    public void validateVerificationOtp(String email, String code) {
        otpService.validateEmailOtp(email, code);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
        sendUserEvent(KafkaEvent.USER_DELETED, user, false);
    }

    public Object validateToken(String token) {
        Authentication authentication = jwtAuthProvider.validateToken(token);
        return authentication.getPrincipal();
    }

    public Object refreshToken(String refreshToken) {
        Authentication authentication = jwtAuthProvider.validateRefreshToken(refreshToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getPrincipal();
        String email = (String) details.get("email");


        User u = userRepository.findUserByEmail(email);
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found");
        }

        CustomUserDetails customUserDetails = buildCustomUserDetails(u);
        return buildTokenResponse(customUserDetails, u);
    }

    // -------------------- Private Helpers --------------------

    private CustomUserDetails buildCustomUserDetails(User u) {
        return CustomUserDetails.builder()
                .email(u.getEmail())
                .id(u.getId())
                .roles(u.getRoles())
                .lang(u.getLang())
                .build();
    }

    private Map<String, Object> buildTokenResponse(CustomUserDetails userDetails, User user) {
        Map<String, Object> tokenResponse = jwtAuthProvider.createToken(userDetails);
        Map<String, Object> refreshTokenResponse = jwtAuthProvider.createRefreshToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", tokenResponse.get("token"));
        response.put("tokenValidity", tokenResponse.get("validity"));
        response.put("refreshToken", refreshTokenResponse.get("refreshToken"));
        response.put("refreshTokenValidity", refreshTokenResponse.get("validity"));
        response.put("user", user);

        return response;
    }

    private void sendUserEvent(KafkaEvent event, User user, boolean active) {
        UserSyncEventDTO payload = UserSyncEventDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .lang(user.getLang())
                .active(active)
                .build();
        kafkaProducer.sendMessage(event.toString(), String.valueOf(user.getId()), payload);
    }
}
