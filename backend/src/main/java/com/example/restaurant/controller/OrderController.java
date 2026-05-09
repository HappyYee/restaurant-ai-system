package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.dto.OrderCreateRequest;
import com.example.restaurant.security.LoginInterceptor;
import com.example.restaurant.service.OrderService;
import com.example.restaurant.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(LoginInterceptor.CURRENT_USER_ID);
        return Result.success(orderService.createOrder(userId, request));
    }

    @GetMapping("/my")
    public Result<List<OrderVO>> listMyOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(LoginInterceptor.CURRENT_USER_ID);
        return Result.success(orderService.listUserOrders(userId));
    }
}
