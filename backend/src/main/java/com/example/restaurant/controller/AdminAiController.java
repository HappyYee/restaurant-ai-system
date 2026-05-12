package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.dto.AiChatRequest;
import com.example.restaurant.security.LoginInterceptor;
import com.example.restaurant.service.AiService;
import com.example.restaurant.vo.AiChatResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai")
public class AdminAiController {
    private final AiService aiService;

    @PostMapping("/business-chat")
    public Result<AiChatResponseVO> businessChat(@Valid @RequestBody AiChatRequest request,
                                                 HttpServletRequest httpRequest) {
        Long adminId = (Long) httpRequest.getAttribute(LoginInterceptor.CURRENT_ADMIN_ID);
        return Result.success(aiService.businessChat(adminId, request));
    }
}
