package com.homosapiens.authservice.core.webConfig.CustomUserDetail;

import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.service.UserLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService  implements UserDetailsService {
    @Autowired
    private UserLookupService userLookupService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Use UserLookupService to handle encrypted email lookup
        // Must throw UsernameNotFoundException (not AppException) so Spring Security
        // properly propagates the error through the login flow.
        User user = userLookupService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        
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
