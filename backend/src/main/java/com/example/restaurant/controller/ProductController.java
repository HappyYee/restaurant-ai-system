package com.example.restaurant.controller;

import com.example.restaurant.common.Result;
import com.example.restaurant.service.ProductService;
import com.example.restaurant.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/products", "/api/dishes"})
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Result<List<ProductVO>> listAvailableProducts() {
        return Result.success(productService.listAvailableProducts());
    }

    @GetMapping("/search")
    public Result<List<ProductVO>> searchProducts(@RequestParam(value = "q", required = false) String keyword) {
        return Result.success(productService.searchAvailableProducts(keyword));
    }

    @GetMapping("/hot")
    public Result<List<ProductVO>> hotProducts(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(productService.listHotProducts(limit));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProductVO(id));
    }
}
