package com.example.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotEmpty
    @Valid
    private List<Item> items;
    private String remark;
    private Integer source = 0;

    @Data
    public static class Item {
        @NotNull
        private Long productId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
