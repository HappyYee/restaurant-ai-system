package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.dto.OrderCreateRequest;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.vo.OrderVO;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Orders> {
    OrderVO createOrder(Long userId, OrderCreateRequest request);

    List<OrderVO> listUserOrders(Long userId);

    List<OrderVO> listAdminOrders(Integer status, Integer source, String keyword);

    Map<String, Object> getOrderStatus(Long userId, Long orderId);

    void updateStatus(Long orderId, Integer status);
}
