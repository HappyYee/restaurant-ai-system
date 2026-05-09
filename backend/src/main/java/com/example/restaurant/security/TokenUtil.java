package com.example.restaurant.security;

import com.example.restaurant.common.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenUtil {
    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public String createAdminToken(Long adminId) {
        return createToken(adminId, TokenRole.ADMIN);
    }

    public String createUserToken(Long userId) {
        return createToken(userId, TokenRole.USER);
    }

    public TokenInfo requireToken(String token, TokenRole role) {
        TokenInfo tokenInfo = tokens.get(token);
        if (tokenInfo == null || tokenInfo.expired()) {
            tokens.remove(token);
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        if (tokenInfo.role() != role) {
            throw new BusinessException(403, "无权限访问");
        }
        return tokenInfo;
    }

    private String createToken(Long userId, TokenRole role) {
        String token = role.name().toLowerCase() + "_" + UUID.randomUUID();
        tokens.put(token, new TokenInfo(userId, role, LocalDateTime.now().plusHours(12)));
        return token;
    }
}
