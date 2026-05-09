package com.example.restaurant.service;

import com.example.restaurant.dto.AdminLoginRequest;
import com.example.restaurant.vo.AdminLoginVO;

public interface AdminAuthService {
    AdminLoginVO login(AdminLoginRequest request);
}
