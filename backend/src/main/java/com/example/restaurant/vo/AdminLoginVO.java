package com.example.restaurant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginVO {
    private String token;
    private Long adminId;
    private String username;
}
