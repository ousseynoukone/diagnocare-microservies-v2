package com.homosapiens.diagnocareservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/swagger")
public class SwaggerController {

    @GetMapping
    public void redirectToSwagger(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/v1/diagnocare/swagger-ui.html");
    }
}

