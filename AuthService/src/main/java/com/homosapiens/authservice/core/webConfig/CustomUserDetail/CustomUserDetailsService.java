package com.homosapiens.authservice.core.webConfig.CustomUserDetail;

import com.homosapiens.authservice.repository.UserRepository;
import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import org.springframework.security.core.userdetails.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService  implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws AppException {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "User Not Found with email: " + email);
        }
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
