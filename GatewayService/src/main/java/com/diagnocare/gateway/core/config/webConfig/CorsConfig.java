package com.diagnocare.gateway.core.config.webConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Must be a specific origin (not "*") when allowCredentials is true.
        // Add any other origins (e.g. staging URL) as needed.
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000"   // fallback (CRA / other)
        ));

        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");

        // Required so the browser sends and receives cookies on cross-origin requests
        corsConfig.setAllowCredentials(true);

        // Expose the Set-Cookie header so the browser can process it
        corsConfig.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
