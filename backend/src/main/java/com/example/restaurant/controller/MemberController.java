package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.security.LoginInterceptor;
import com.example.restaurant.service.MemberService;
import com.example.restaurant.vo.MemberProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/profile")
    public Result<MemberProfileVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.CURRENT_USER_ID);
        return Result.success(memberService.getProfile(userId));
    }
}
