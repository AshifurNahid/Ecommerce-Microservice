package com.nahid.product.service;

import com.nahid.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main product service interface that delegates to specialized services
 */
public interface ProductService {
    // CRUD operations
    ProductResponseDto createProduct(CreateProductRequestDto request);
    ProductResponseDto getProductById(Long id);
    ProductResponseDto getProductBySku(String sku);
    Page<ProductResponseDto> getAllProducts(Pageable pageable);
    ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request);
    void deleteProduct(Long id);

    // Search operations
    Page<ProductResponseDto> getActiveProducts(Pageable pageable);
    Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<ProductResponseDto> searchProducts(String name, String brand, BigDecimal minPrice,
                                         BigDecimal maxPrice, Long categoryId, Pageable pageable);
    List<ProductResponseDto> getFeaturedProducts();
    List<ProductResponseDto> getLowStockProducts();

    // Inventory operations
    ProductResponseDto updateStock(Long id, Integer newStock);
    boolean isProductAvailable(Long id, Integer requiredQuantity);

    // Purchase operations
    PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request);
}
