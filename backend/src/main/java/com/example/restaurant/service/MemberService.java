package com.example.restaurant.service;

import com.example.restaurant.vo.MemberProfileVO;

import java.util.List;
import java.util.Map;

public interface MemberService {
    MemberProfileVO getProfile(Long userId);

    List<MemberProfileVO> listMembers();

    Map<String, Object> getMemberStats();
}
