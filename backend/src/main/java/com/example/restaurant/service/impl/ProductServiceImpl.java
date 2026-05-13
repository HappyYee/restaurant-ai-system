package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.dto.ProductSaveRequest;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.ProductService;
import com.example.restaurant.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ProductVO> listAvailableProducts() {
        return searchAvailableProducts(null);
    }

    @Override
    public List<ProductVO> searchAvailableProducts(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .gt(Product::getStock, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(item -> item.like(Product::getName, keyword)
                    .or()
                    .like(Product::getTasteTags, keyword)
                    .or()
                    .like(Product::getCategory, keyword));
        }
        wrapper.orderByAsc(Product::getCategory).orderByDesc(Product::getId);
        return list(wrapper).stream().map(ProductVO::from).toList();
    }

    @Override
    public List<ProductVO> listHotProducts(Integer limit) {
        int size = Math.max(1, Math.min(limit == null ? 5 : limit, 12));
        List<Long> hotIds = jdbcTemplate.queryForList("""
                SELECT p.id
                FROM product p
                LEFT JOIN order_item oi ON oi.product_id = p.id
                LEFT JOIN orders o ON o.id = oi.order_id AND o.status <> 3
                WHERE p.status = 1 AND p.stock > 0 AND p.deleted = 0
                GROUP BY p.id
                ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
                LIMIT ?
                """, Long.class, size);
        Map<Long, Product> productMap = listByIds(hotIds).stream()
                .collect(LinkedHashMap::new, (map, product) -> map.put(product.getId(), product), LinkedHashMap::putAll);
        List<ProductVO> result = new ArrayList<>();
        for (Long id : hotIds) {
            Product product = productMap.get(id);
            if (product != null) {
                result.add(ProductVO.from(product));
            }
        }
        if (result.size() < size) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1)
                    .gt(Product::getStock, 0)
                    .orderByDesc(Product::getId)
                    .last("LIMIT " + (size - result.size()));
            if (!hotIds.isEmpty()) {
                wrapper.notIn(Product::getId, hotIds);
            }
            result.addAll(list(wrapper).stream().map(ProductVO::from).toList());
        }
        return result;
    }

    @Override
    public List<ProductVO> listAdminProducts(String keyword, String category, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(item -> item.like(Product::getName, keyword)
                    .or()
                    .like(Product::getTasteTags, keyword)
                    .or()
                    .like(Product::getCategory, keyword));
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getId);
        return list(wrapper).stream().map(ProductVO::from).toList();
    }

    @Override
    public ProductVO getProductVO(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("菜品不存在");
        }
        return ProductVO.from(product);
    }

    @Override
    public ProductVO createProduct(ProductSaveRequest request) {
        Product product = toEntity(new Product(), request);
        save(product);
        return ProductVO.from(product);
    }

    @Override
    public ProductVO updateProduct(Long id, ProductSaveRequest request) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("菜品不存在");
        }
        updateById(toEntity(product, request));
        return ProductVO.from(getById(id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("菜品不存在");
        }
        product.setStatus(status);
        updateById(product);
    }

    private Product toEntity(Product product, ProductSaveRequest request) {
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice() == null ? BigDecimal.ZERO : request.getCostPrice());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        product.setTasteTags(request.getTasteTags());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setCookTime(request.getCookTime() == null ? 10 : request.getCookTime());
        return product;
    }
}
