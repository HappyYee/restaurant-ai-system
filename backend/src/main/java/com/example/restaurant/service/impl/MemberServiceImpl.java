package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.entity.User;
import com.example.restaurant.mapper.UserMapper;
import com.example.restaurant.service.MemberService;
import com.example.restaurant.vo.MemberProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final UserMapper userMapper;

    @Override
    public MemberProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "会员不存在");
        }
        return MemberProfileVO.from(user);
    }

    @Override
    public List<MemberProfileVO> listMembers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getTotalSpent))
                .stream()
                .map(MemberProfileVO::from)
                .toList();
    }

    @Override
    public Map<String, Object> getMemberStats() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>());
        BigDecimal totalSpent = users.stream()
                .map(user -> user.getTotalSpent() == null ? BigDecimal.ZERO : user.getTotalSpent())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalPoints = users.stream().mapToInt(user -> user.getPoints() == null ? 0 : user.getPoints()).sum();
        Map<String, Long> levelDistribution = users.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getMemberLevel() == null ? "普通会员" : user.getMemberLevel(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberCount", users.size());
        result.put("totalMemberSpent", totalSpent);
        result.put("avgMemberSpent", users.isEmpty()
                ? BigDecimal.ZERO
                : totalSpent.divide(BigDecimal.valueOf(users.size()), 2, java.math.RoundingMode.HALF_UP));
        result.put("totalPoints", totalPoints);
        result.put("levelDistribution", levelDistribution);
        result.put("topMembers", users.stream()
                .sorted((a, b) -> (b.getTotalSpent() == null ? BigDecimal.ZERO : b.getTotalSpent())
                        .compareTo(a.getTotalSpent() == null ? BigDecimal.ZERO : a.getTotalSpent()))
                .limit(8)
                .map(MemberProfileVO::from)
                .toList());
        return result;
    }
}
