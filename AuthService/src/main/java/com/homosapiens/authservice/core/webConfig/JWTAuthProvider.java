package com.homosapiens.authservice.core.webConfig;


import com.homosapiens.authservice.core.exception.AppException;
import com.homosapiens.authservice.core.webConfig.CustomUserDetail.CustomUserDetailsService;
import com.homosapiens.authservice.model.Role;
import com.homosapiens.authservice.model.dtos.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class JWTAuthProvider {
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.refresh-secret-key:refresh-secret-key}")
    private String refreshSecretKey;

    @Getter
    @Value("${security.jwt.token.expiration:3600000}")
    private long accessTokenExpiration;

    @Getter
    @Value("${security.jwt.token.refresh-expiration:86400000}")
    private long refreshTokenExpiration;

    @Autowired
    private CustomUserDetailsService customUserDetails;


    public Map<String, Object> createToken(CustomUserDetails user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenExpiration);

        List<String> roleNames = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());




        // Create the JWT access token
        String lang = user.getLang() != null ? user.getLang() : "fr";

        String token = JWT.create()
                .withIssuer(user.getEmail())
                .withClaim("id", user.getId())
                .withClaim("roles", roleNames)
                .withClaim("lang", lang)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .sign(Algorithm.HMAC256(secretKey));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("validity", validity);

        return response;
    }


    public Map<String, Object> createRefreshToken(CustomUserDetails user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpiration);

        List<String> roleNames = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        String lang = user.getLang() != null ? user.getLang() : "fr";

        String refreshToken = JWT.create()
                .withIssuer(user.getEmail())
                .withClaim("id", user.getId())
                .withClaim("roles", roleNames)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("tokenType", "refresh")
                .withClaim("lang", lang)
                .sign(Algorithm.HMAC256(refreshSecretKey));

        Map<String, Object> response = new HashMap<>();
        response.put("refreshToken", refreshToken);
        response.put("validity", validity);

        return response;
    }


    public Authentication validateToken(String token) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decoded = verifier.verify(token);



            // Check if token is expired
            if (decoded.getExpiresAt().before(new Date())) {
                throw new AppException(HttpStatus.UNAUTHORIZED,"Token has expired. Please login again.");
            }
            List<String> roles = decoded.getClaim("roles").asList(String.class);

            if (roles == null) {
                roles = new ArrayList<>();
            }
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", decoded.getClaim("id").asLong());
            userData.put("email", decoded.getIssuer());
            userData.put("roles", roles);
            String lang = decoded.getClaim("lang").asString();
            userData.put("lang", lang != null ? lang : "fr");
            UserDetails userDetails = customUserDetails.loadUserByUsername(decoded.getIssuer());

            return new UsernamePasswordAuthenticationToken(userData, userDetails.getPassword(), userDetails.getAuthorities());
        } catch (JWTVerificationException e) {
            throw new AppException( HttpStatus.UNAUTHORIZED,e.getMessage());
        }
    }

    public Authentication validateRefreshToken(String refreshToken) {
        System.out.println(refreshToken);
        try {
            Algorithm algorithm = Algorithm.HMAC256(refreshSecretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decoded = verifier.verify(refreshToken);


            // Check if token is expired
            if (decoded.getExpiresAt().before(new Date())) {
                throw new AppException(HttpStatus.UNAUTHORIZED,"Refresh token has expired. Please login again.");
            }

            // Verify it's a refresh token
            if (!"refresh".equals(decoded.getClaim("tokenType").asString())) {
                throw new AppException(HttpStatus.UNAUTHORIZED,"Invalid token type. Please provide a refresh token.");
            }

            List<String> roles = decoded.getClaim("roles").asList(String.class);
            if (roles == null) {
                roles = new ArrayList<>();
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", decoded.getClaim("id").asLong());
            userData.put("email", decoded.getIssuer());
            userData.put("roles", roles);
            String lang = decoded.getClaim("lang").asString();
            userData.put("lang", lang != null ? lang : "fr");
            UserDetails userDetails = customUserDetails.loadUserByUsername(decoded.getIssuer());

            return new UsernamePasswordAuthenticationToken(userData, userDetails.getPassword(), userDetails.getAuthorities());
        } catch (JWTVerificationException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED,"Invalid refresh token. Please provide a valid refresh token.");

        }
    }


}
