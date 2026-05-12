package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.service.MemberService;
import com.example.restaurant.vo.MemberProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class AdminMemberController {
    private final MemberService memberService;

    @GetMapping
    public Result<List<MemberProfileVO>> listMembers() {
        return Result.success(memberService.listMembers());
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(memberService.getMemberStats());
    }
}
