package com.homosapiens.authservice.core.webConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    JWTAuthProvider JwtAuthProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){

        try {
            http.csrf(AbstractHttpConfigurer::disable)
                    .addFilterBefore(new JwAuthFilter(JwtAuthProvider), BasicAuthenticationFilter.class)
                    .sessionManagement(customizer -> customizer.
                            sessionCreationPolicy( SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests((requests)->requests
                            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                                    .requestMatchers(HttpMethod.POST,"/login", "/validateToken", "/register","refresh-token").permitAll()
                            .anyRequest().authenticated());

            return  http.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
