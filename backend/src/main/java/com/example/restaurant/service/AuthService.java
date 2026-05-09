package com.example.restaurant.service;

import com.example.restaurant.dto.WxLoginRequest;
import com.example.restaurant.vo.WxLoginVO;

public interface AuthService {
    WxLoginVO wxLogin(WxLoginRequest request);
}
