package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.dto.WxLoginRequest;
import com.example.restaurant.service.AuthService;
import com.example.restaurant.vo.WxLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/wx-login")
    public Result<WxLoginVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return Result.success(authService.wxLogin(request));
    }
}
