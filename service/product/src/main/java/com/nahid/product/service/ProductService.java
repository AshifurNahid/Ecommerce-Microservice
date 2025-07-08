package com.nahid.product.service;


import com.nahid.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(CreateProductRequestDto request);

    ProductResponseDto getProductById(Long id);

    ProductResponseDto getProductBySku(String sku);

    Page<ProductResponseDto> getAllProducts(Pageable pageable);

    Page<ProductResponseDto> getActiveProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponseDto> searchProducts(String name, String brand, BigDecimal minPrice,
                                         BigDecimal maxPrice, Long categoryId, Pageable pageable);

    List<ProductResponseDto> getFeaturedProducts();

    List<ProductResponseDto> getLowStockProducts();

    ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request);

    void deleteProduct(Long id);

    ProductResponseDto updateStock(Long id, Integer newStock);

    boolean isProductAvailable(Long id, Integer requiredQuantity);

    PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request);

}
