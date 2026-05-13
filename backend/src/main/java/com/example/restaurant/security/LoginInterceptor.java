package com.example.restaurant.security;

import com.example.restaurant.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    public static final String CURRENT_ADMIN_ID = "currentAdminId";
    public static final String CURRENT_USER_ID = "currentUserId";

    private final TokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/health") || path.startsWith("/api/admin/auth/login") || path.startsWith("/api/auth/wx-login")
                || path.startsWith("/api/products") || path.startsWith("/api/dishes")) {
            return true;
        }

        if (path.startsWith("/api/admin")) {
            TokenInfo tokenInfo = tokenUtil.requireToken(resolveToken(request), TokenRole.ADMIN);
            request.setAttribute(CURRENT_ADMIN_ID, tokenInfo.userId());
            return true;
        }

        if (path.startsWith("/api/orders") || path.startsWith("/api/member") || path.startsWith("/api/ai")) {
            TokenInfo tokenInfo = tokenUtil.requireToken(resolveToken(request), TokenRole.USER);
            request.setAttribute(CURRENT_USER_ID, tokenInfo.userId());
            return true;
        }

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        String token = request.getHeader("token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        throw new BusinessException(401, "请先登录");
    }
}
