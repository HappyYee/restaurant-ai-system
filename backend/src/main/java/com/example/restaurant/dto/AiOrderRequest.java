package com.example.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiOrderRequest {
    private String sessionId;
    @NotBlank
    private String message;
}
