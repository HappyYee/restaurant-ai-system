package com.example.restaurant.vo;

import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private Integer source;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;

    public static OrderVO from(Orders order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setSource(order.getSource());
        vo.setCreateTime(order.getCreateTime());
        vo.setItems(items.stream().map(OrderItemVO::from).toList());
        return vo;
    }
}
