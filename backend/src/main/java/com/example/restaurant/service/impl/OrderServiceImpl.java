package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.dto.OrderCreateRequest;
import com.example.restaurant.entity.OrderItem;
import com.example.restaurant.entity.Orders;
import com.example.restaurant.entity.Product;
import com.example.restaurant.entity.User;
import com.example.restaurant.mapper.OrderItemMapper;
import com.example.restaurant.mapper.OrdersMapper;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.mapper.UserMapper;
import com.example.restaurant.service.OrderService;
import com.example.restaurant.vo.OrderVO;
import com.example.restaurant.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {
    private static final BigDecimal POINT_VALUE = new BigDecimal("0.01");
    private static final int POINT_STEP = 50;

    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal originalAmount = BigDecimal.ZERO;
        BigDecimal estimatedCost = BigDecimal.ZERO;

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

            BigDecimal unitPrice = ProductVO.calcMemberPrice(product);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
            originalAmount = originalAmount.add(subtotal);
            estimatedCost = estimatedCost.add(estimateProductCost(product).multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productMapper.updateById(product);
        }

        PointsRedemption redemption = calculateRedemption(user, request.getRedeemPoints(), originalAmount, estimatedCost);
        BigDecimal totalAmount = originalAmount.subtract(redemption.discount()).setScale(2, RoundingMode.HALF_UP);
        int pointsEarned = calculateEarnedPoints(user.getMemberLevel(), totalAmount);

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOriginalAmount(originalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setTotalAmount(totalAmount);
        order.setPointsUsed(redemption.pointsUsed());
        order.setPointsDiscount(redemption.discount());
        order.setPointsEarned(pointsEarned);
        order.setStatus(0);
        order.setRemark(request.getRemark());
        order.setSource(request.getSource() == null ? 0 : request.getSource());
        baseMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);
        }

        refreshMemberAfterPaidOrder(user, totalAmount, redemption.pointsUsed(), pointsEarned);

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
    public Map<String, Object> getOrderStatus(Long userId, Long orderId) {
        Orders order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("statusText", statusText(order.getStatus()));
        result.put("updateTime", order.getUpdateTime());
        return result;
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

    private String statusText(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "制作中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "待处理";
        };
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

    private void refreshMemberAfterPaidOrder(User user, BigDecimal totalAmount, int pointsUsed, int pointsEarned) {
        BigDecimal currentSpent = user.getTotalSpent() == null ? BigDecimal.ZERO : user.getTotalSpent();
        BigDecimal nextSpent = currentSpent.add(totalAmount).setScale(2, RoundingMode.HALF_UP);
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        user.setTotalSpent(nextSpent);
        user.setPoints(Math.max(0, currentPoints - pointsUsed) + pointsEarned);
        user.setMemberLevel(resolveMemberLevel(nextSpent));
        if (user.getMemberSince() == null) {
            user.setMemberSince(LocalDateTime.now());
        }
        userMapper.updateById(user);
    }

    private String resolveMemberLevel(BigDecimal totalSpent) {
        if (totalSpent.compareTo(new BigDecimal("300")) >= 0) {
            return "金卡会员";
        }
        if (totalSpent.compareTo(new BigDecimal("100")) >= 0) {
            return "银卡会员";
        }
        return "普通会员";
    }

    private PointsRedemption calculateRedemption(User user, Integer requestedPoints,
                                                 BigDecimal originalAmount, BigDecimal estimatedCost) {
        int requested = requestedPoints == null ? 0 : Math.max(0, requestedPoints);
        if (requested == 0 || originalAmount.signum() <= 0) {
            return new PointsRedemption(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        int availablePoints = user.getPoints() == null ? 0 : Math.max(0, user.getPoints());
        int normalizedRequested = floorToStep(Math.min(requested, availablePoints));
        if (normalizedRequested <= 0) {
            return new PointsRedemption(0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal levelCap = BigDecimal.valueOf(levelRedeemCapPoints(user.getMemberLevel())).multiply(POINT_VALUE);
        BigDecimal orderCap = originalAmount.multiply(new BigDecimal("0.10"));
        BigDecimal profitCap = originalAmount.subtract(estimatedCost.multiply(new BigDecimal("1.20")));
        if (profitCap.signum() < 0) {
            profitCap = BigDecimal.ZERO;
        }
        BigDecimal maxDiscount = min(levelCap, orderCap, profitCap).setScale(2, RoundingMode.DOWN);
        int maxPoints = floorToStep(maxDiscount.divide(POINT_VALUE, 0, RoundingMode.DOWN).intValue());
        int pointsUsed = Math.min(normalizedRequested, maxPoints);
        BigDecimal discount = BigDecimal.valueOf(pointsUsed).multiply(POINT_VALUE).setScale(2, RoundingMode.HALF_UP);
        return new PointsRedemption(pointsUsed, discount);
    }

    private int calculateEarnedPoints(String memberLevel, BigDecimal paidAmount) {
        BigDecimal multiplier;
        if ("金卡会员".equals(memberLevel)) {
            multiplier = new BigDecimal("1.50");
        } else if ("银卡会员".equals(memberLevel)) {
            multiplier = new BigDecimal("1.20");
        } else {
            multiplier = BigDecimal.ONE;
        }
        return paidAmount.multiply(multiplier).setScale(0, RoundingMode.DOWN).intValue();
    }

    private int levelRedeemCapPoints(String memberLevel) {
        if ("金卡会员".equals(memberLevel)) {
            return 1500;
        }
        if ("银卡会员".equals(memberLevel)) {
            return 1000;
        }
        return 500;
    }

    private int floorToStep(int points) {
        return points / POINT_STEP * POINT_STEP;
    }

    private BigDecimal estimateProductCost(Product product) {
        BigDecimal cost = product.getCostPrice();
        if (cost == null || cost.signum() <= 0) {
            BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            return price.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
        }
        return cost;
    }

    private BigDecimal min(BigDecimal first, BigDecimal second, BigDecimal third) {
        return first.min(second).min(third);
    }

    private record PointsRedemption(int pointsUsed, BigDecimal discount) {
    }
}
