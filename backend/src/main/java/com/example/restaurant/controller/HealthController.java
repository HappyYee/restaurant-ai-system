package com.example.restaurant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    private static final Map<String, String> OK = Map.of("status", "ok");

    @GetMapping({"/health", "/api/health"})
    public Map<String, String> health() {
        return OK;
    }
}
