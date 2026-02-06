package com.homosapiens.diagnocareservice.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MLServiceConfig {

    @Value("${ml.service.url:http://ml-prediction-service}")
    private String mlServiceUrl;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getMlServiceUrl() {
        return mlServiceUrl;
    }
}
