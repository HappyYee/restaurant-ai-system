package com.example.restaurant.vo;

import com.example.restaurant.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberProfileVO {
    private Long userId;
    private String nickname;
    private String memberLevel;
    private Integer points;
    private BigDecimal totalSpent;
    private LocalDateTime memberSince;
    private BigDecimal nextLevelNeed;

    public static MemberProfileVO from(User user) {
        MemberProfileVO vo = new MemberProfileVO();
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setMemberLevel(user.getMemberLevel() == null ? "普通会员" : user.getMemberLevel());
        vo.setPoints(user.getPoints() == null ? 0 : user.getPoints());
        vo.setTotalSpent(user.getTotalSpent() == null ? BigDecimal.ZERO : user.getTotalSpent());
        vo.setMemberSince(user.getMemberSince());
        vo.setNextLevelNeed(nextLevelNeed(vo.getTotalSpent()));
        return vo;
    }

    private static BigDecimal nextLevelNeed(BigDecimal totalSpent) {
        BigDecimal spent = totalSpent == null ? BigDecimal.ZERO : totalSpent;
        if (spent.compareTo(new BigDecimal("300")) >= 0) {
            return BigDecimal.ZERO;
        }
        if (spent.compareTo(new BigDecimal("100")) >= 0) {
            return new BigDecimal("300").subtract(spent);
        }
        return new BigDecimal("100").subtract(spent);
    }
}
