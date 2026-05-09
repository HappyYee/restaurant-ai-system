package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.dto.AdminLoginRequest;
import com.example.restaurant.entity.AdminUser;
import com.example.restaurant.mapper.AdminUserMapper;
import com.example.restaurant.security.TokenUtil;
import com.example.restaurant.service.AdminAuthService;
import com.example.restaurant.vo.AdminLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    @Override
    public AdminLoginVO login(AdminLoginRequest request) {
        AdminUser adminUser = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, request.getUsername())
        );
        if (adminUser == null || adminUser.getStatus() == null || adminUser.getStatus() != 1) {
            throw new BusinessException("账号不存在或已停用");
        }
        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        return new AdminLoginVO(tokenUtil.createAdminToken(adminUser.getId()), adminUser.getId(), adminUser.getUsername());
    }
}
