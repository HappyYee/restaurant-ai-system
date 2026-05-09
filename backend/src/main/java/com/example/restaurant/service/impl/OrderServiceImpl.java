package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.dto.OrderCreateRequest;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.OrderService;
import com.example.restaurant.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderCreateRequest.Item itemRequest : request.getItems()) {
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException("菜品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException("菜品已下架：" + product.getName());
            }
            if (product.getStock() == null || product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException("库存不足：" + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productMapper.updateById(product);
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setRemark(request.getRemark());
        order.setSource(request.getSource() == null ? 0 : request.getSource());
        baseMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        return OrderVO.from(order, orderItems);
    }

    @Override
    public List<OrderVO> listUserOrders(Long userId) {
        List<Orders> orders = lambdaQuery()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime)
                .list();
        return orders.stream().map(this::toVO).toList();
    }

    @Override
    public List<OrderVO> listAdminOrders(Integer status, Integer source, String keyword) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Orders::getStatus, status);
        }
        if (source != null) {
            wrapper.eq(Orders::getSource, source);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Orders::getOrderNo, keyword).or().like(Orders::getRemark, keyword);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return list(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        updateById(order);
    }

    private OrderVO toVO(Orders order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        return OrderVO.from(order, items);
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "RO" + time + suffix;
    }
}
