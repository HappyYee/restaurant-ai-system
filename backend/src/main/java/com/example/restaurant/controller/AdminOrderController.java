package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.dto.OrderStatusUpdateRequest;
import com.example.restaurant.service.OrderService;
import com.example.restaurant.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    public Result<List<OrderVO>> listOrders(Integer status, Integer source, String keyword) {
        return Result.success(orderService.listAdminOrders(status, source, keyword));
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        orderService.updateStatus(id, request.getStatus());
        return Result.success();
    }
}
