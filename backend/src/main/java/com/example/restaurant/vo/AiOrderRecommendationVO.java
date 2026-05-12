package com.example.restaurant.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiOrderRecommendationVO {
    private String sessionId;
    private List<String> thinking = new ArrayList<>();
    private List<Plan> plans = new ArrayList<>();

    @Data
    public static class Plan {
        private String name;
        private String reason;
        private List<Item> items = new ArrayList<>();
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }

    @Data
    public static class Item {
        private Long productId;
        private String productName;
        private String name;
        private String category;
        private Integer quantity = 1;
        private BigDecimal unitPrice = BigDecimal.ZERO;
        private BigDecimal subtotal = BigDecimal.ZERO;
        private Integer cookTime;
    }
}
