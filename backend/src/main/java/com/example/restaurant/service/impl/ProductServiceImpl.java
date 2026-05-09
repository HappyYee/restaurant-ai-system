package com.example.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.restaurant.common.BusinessException;
import com.example.restaurant.dto.ProductSaveRequest;
import com.example.restaurant.entity.Product;
import com.example.restaurant.mapper.ProductMapper;
import com.example.restaurant.service.ProductService;
import com.example.restaurant.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Override
    public List<ProductVO> listAvailableProducts() {
        return lambdaQuery()
                .eq(Product::getStatus, 1)
                .gt(Product::getStock, 0)
                .orderByAsc(Product::getCategory)
                .orderByDesc(Product::getId)
                .list()
                .stream()
                .map(ProductVO::from)
                .toList();
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
