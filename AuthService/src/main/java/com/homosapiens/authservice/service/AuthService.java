package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.webConfig.JWTAuthProvider;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import com.homosapiens.authservice.model.dtos.UserLoginDto;
import com.homosapiens.authservice.model.dtos.UserRegisterDto;
import com.homosapiens.authservice.model.mapper.UserMapper;
import com.homosapiens.authservice.repository.RoleRepository;
import com.homosapiens.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

    public Object login(UserLoginDto loginDto) {
        User u = userRepository.findUserByEmail(loginDto.getEmail());
        if (u == null) {
            throw new AppException(HttpStatus.NOT_FOUND,"User not found");
        }

        if (!passwordEncoder.matches(loginDto.getPassword() , u.getPassword())) {
            throw new AppException( HttpStatus.UNAUTHORIZED,"Email or password incorrect" );
        }

        CustomUserDetails customUserDetails = CustomUserDetails.builder()
                .email(u.getEmail())
                .id(u.getId())
                .roles(u.getRoles())
                .build();

        Map<String, Object> tokenResponse = jwtAuthProvider.createToken(customUserDetails);
        Map<String, Object> refreshTokenResponse = jwtAuthProvider.createRefreshToken(customUserDetails);

        // Extract token and validity from the responses
        String token = (String) tokenResponse.get("token");
        Date tokenValidity = (Date) tokenResponse.get("validity");

        String refreshToken = (String) refreshTokenResponse.get("refreshToken");
        Date refreshTokenValidity = (Date) refreshTokenResponse.get("validity");


        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tokenValidity", tokenValidity);
        response.put("refreshToken", refreshToken);
        response.put("refreshTokenValidity", refreshTokenValidity);

        response.put("user", u);

        return response;
    }

    public User register(UserRegisterDto user) {
        String password = passwordEncoder.encode(user.getPassword()) ;
        user.setPassword(password);

        Optional<Role> role = roleRepository.findById(user.getRoleId());
        if(role.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND,"Role not found");
        }

        User realUser = UserMapper.toEntity(user,role.get());

        this.userRepository.save(realUser);

        return realUser;
    }


}
