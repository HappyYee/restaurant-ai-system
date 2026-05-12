package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.dto.WxLoginRequest;
import com.example.restaurant.entity.User;
import com.example.restaurant.mapper.UserMapper;
import com.example.restaurant.security.TokenUtil;
import com.example.restaurant.service.AuthService;
import com.example.restaurant.vo.WxLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final TokenUtil tokenUtil;

    @Override
    public WxLoginVO wxLogin(WxLoginRequest request) {
        // 开发阶段没有微信 appid/secret 时，用 code 稳定生成 mock openid；后续可替换为真实微信接口调用。
        String openid = "mock_" + UUID.nameUUIDFromBytes(request.getCode().getBytes(StandardCharsets.UTF_8));
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            initMemberFields(user);
            userMapper.insert(user);
        } else if (user.getMemberLevel() == null || user.getPoints() == null || user.getTotalSpent() == null
                || user.getMemberSince() == null) {
            initMemberFields(user);
            userMapper.updateById(user);
        }
        return new WxLoginVO(tokenUtil.createUserToken(user.getId()), user.getId(), user.getNickname());
    }

    private void initMemberFields(User user) {
        if (user.getMemberLevel() == null) {
            user.setMemberLevel("普通会员");
        }
        if (user.getPoints() == null) {
            user.setPoints(0);
        }
        if (user.getTotalSpent() == null) {
            user.setTotalSpent(BigDecimal.ZERO);
        }
        if (user.getMemberSince() == null) {
            user.setMemberSince(LocalDateTime.now());
        }
    }
}
