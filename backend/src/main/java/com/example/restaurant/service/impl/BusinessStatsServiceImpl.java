package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.BusinessStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessStatsServiceImpl implements BusinessStatsService {
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @Override
    public Map<String, Object> dashboard() {
        LocalDate today = LocalDate.now();
        List<Orders> todayOrders = findOrdersByDate(today);
        List<Orders> yesterdayOrders = findOrdersByDate(today.minusDays(1));
        BigDecimal todayRevenue = sumOrderAmount(todayOrders);
        BigDecimal yesterdayRevenue = sumOrderAmount(yesterdayOrders);
        List<Product> lowStock = productMapper.selectList(new LambdaQueryWrapper<Product>().le(Product::getStock, 10));

        Map<String, Integer> productQuantity = new HashMap<>();
        for (Orders order : todayOrders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            for (OrderItem item : items) {
                productQuantity.merge(item.getProductName(), item.getQuantity(), Integer::sum);
            }
        }

        List<Map<String, Object>> topProducts = productQuantity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("quantity", entry.getValue());
                    return item;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayRevenue", todayRevenue);
        result.put("yesterdayRevenue", yesterdayRevenue);
        result.put("todayOrderCount", todayOrders.size());
        result.put("yesterdayOrderCount", yesterdayOrders.size());
        result.put("pendingOrderCount", todayOrders.stream().filter(order -> order.getStatus() == 0).count());
        result.put("lowStockCount", lowStock.size());
        result.put("lowStockProducts", lowStock.stream()
                .sorted(Comparator.comparing(Product::getStock))
                .map(Product::getName)
                .toList());
        result.put("topProducts", topProducts);
        return result;
    }

    private List<Orders> findOrdersByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, start)
                .lt(Orders::getCreateTime, end)
                .ne(Orders::getStatus, 3));
    }

    private BigDecimal sumOrderAmount(List<Orders> orders) {
        return orders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
