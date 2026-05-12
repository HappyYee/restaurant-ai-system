package com.example.restaurant.vo;

import com.example.restaurant.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private String memberDiscountLabel;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer status;
    private String tasteTags;
    private String description;
    private String imageUrl;
    private Integer cookTime;

    public static ProductVO from(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCategory(product.getCategory());
        vo.setPrice(product.getPrice());
        vo.setMemberPrice(calcMemberPrice(product));
        vo.setMemberDiscountLabel("饮品".equals(product.getCategory()) ? "会员85折" : "会员9折");
        vo.setCostPrice(product.getCostPrice());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setTasteTags(product.getTasteTags());
        vo.setDescription(product.getDescription());
        vo.setImageUrl(product.getImageUrl());
        vo.setCookTime(product.getCookTime());
        return vo;
    }

    public static BigDecimal calcMemberPrice(Product product) {
        BigDecimal discount = "饮品".equals(product.getCategory()) ? new BigDecimal("0.85") : new BigDecimal("0.90");
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        return price.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
