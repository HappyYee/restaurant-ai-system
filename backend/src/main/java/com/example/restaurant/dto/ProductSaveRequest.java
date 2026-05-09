package com.example.restaurant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSaveRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String category;
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
    @DecimalMin("0.00")
    private BigDecimal costPrice;
    @NotNull
    @Min(0)
    private Integer stock;
    private Integer status = 1;
    private String tasteTags;
    private String description;
    private String imageUrl;
    @Min(1)
    private Integer cookTime = 10;
}
