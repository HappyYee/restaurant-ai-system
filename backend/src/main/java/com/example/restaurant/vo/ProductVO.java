package com.example.restaurant.vo;

import com.example.restaurant.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
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
        vo.setCostPrice(product.getCostPrice());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus());
        vo.setTasteTags(product.getTasteTags());
        vo.setDescription(product.getDescription());
        vo.setImageUrl(product.getImageUrl());
        vo.setCookTime(product.getCookTime());
        return vo;
    }
}
