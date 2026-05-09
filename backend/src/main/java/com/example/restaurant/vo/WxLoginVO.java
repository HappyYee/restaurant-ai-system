package com.example.restaurant.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WxLoginVO {
    private String token;
    private Long userId;
    private String nickname;
}
