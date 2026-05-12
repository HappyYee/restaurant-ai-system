package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.dto.AiOrderRequest;
import com.example.restaurant.security.LoginInterceptor;
import com.example.restaurant.service.AiService;
import com.example.restaurant.vo.AiOrderRecommendationVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiOrderController {
    private final AiService aiService;

    @PostMapping("/order-recommend")
    public Result<AiOrderRecommendationVO> recommendOrder(@Valid @RequestBody AiOrderRequest request,
                                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(LoginInterceptor.CURRENT_USER_ID);
        return Result.success(aiService.recommendOrder(userId, request));
    }
}
