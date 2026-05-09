package com.example.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {
    @NotBlank
    private String code;
    private String nickname;
    private String avatarUrl;
}
