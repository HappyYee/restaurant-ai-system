package com.example.restaurant.service;

import com.example.restaurant.dto.AiChatRequest;
import com.example.restaurant.dto.AiOrderRequest;
import com.example.restaurant.vo.AiChatResponseVO;
import com.example.restaurant.vo.AiOrderRecommendationVO;

public interface AiService {
    AiChatResponseVO businessChat(Long adminId, AiChatRequest request);

    AiOrderRecommendationVO recommendOrder(Long userId, AiOrderRequest request);
}
