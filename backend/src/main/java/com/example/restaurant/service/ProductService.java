package com.example.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.restaurant.dto.ProductSaveRequest;
import com.example.restaurant.entity.Product;
import com.example.restaurant.vo.ProductVO;

import java.util.List;

public interface ProductService extends IService<Product> {
    List<ProductVO> listAvailableProducts();

    List<ProductVO> searchAvailableProducts(String keyword);

    List<ProductVO> listHotProducts(Integer limit);

    List<ProductVO> listAdminProducts(String keyword, String category, Integer status);

    ProductVO getProductVO(Long id);

    ProductVO createProduct(ProductSaveRequest request);

    ProductVO updateProduct(Long id, ProductSaveRequest request);

    void updateStatus(Long id, Integer status);
}
