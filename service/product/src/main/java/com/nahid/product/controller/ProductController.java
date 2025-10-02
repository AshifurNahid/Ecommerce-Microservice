package com.nahid.product.controller;

import com.nahid.product.dto.*;
import com.nahid.product.dto.response.ApiResponse;
import com.nahid.product.service.ProductService;
import com.nahid.product.util.constant.ApiResponseConstant;
import com.nahid.product.util.helper.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        log.info("Received request to create product with SKU: {}", request.getSku());
        ProductResponseDto response = productService.createProduct(request);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_CREATED_SUCCESSFULLY, HttpStatus.CREATED);
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchaseProductResponseDto>> purchaseProduct(@Valid @RequestBody PurchaseProductRequestDto request) {
        log.info("Received purchase request for order reference: {}", request.getOrderReference());
        PurchaseProductResponseDto response = productService.processPurchase(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.CONFLICT;
        if (response.isSuccess()) {
            return ApiResponseUtil.success(response, response.getMessage(), status);
        }
        return ApiResponseUtil.failureWithData(response, response.getMessage(), status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with ID: {}", id);
        ProductResponseDto response = productService.getProductById(id);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySku(@PathVariable String sku) {
        log.info("Received request to get product with SKU: {}", sku);
        ProductResponseDto response = productService.getProductBySku(sku);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_FETCHED_SUCCESSFULLY);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get all products with pagination: {}", pageable);
        Page<ProductResponseDto> response = productService.getAllProducts(pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCTS_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getActiveProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get active products with pagination: {}", pageable);
        Page<ProductResponseDto> response = productService.getActiveProducts(pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.ACTIVE_PRODUCTS_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get products by category ID: {} with pagination: {}", categoryId, pageable);
        Page<ProductResponseDto> response = productService.getProductsByCategory(categoryId, pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCTS_BY_CATEGORY_FETCHED);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to search products with filters");
        Page<ProductResponseDto> response = productService.searchProducts(name, brand, minPrice, maxPrice, categoryId, pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCTS_SEARCHED_SUCCESSFULLY);
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getFeaturedProducts() {
        log.info("Received request to get featured products");
        List<ProductResponseDto> response = productService.getFeaturedProducts();
        return ApiResponseUtil.success(response, ApiResponseConstant.FEATURED_PRODUCTS_FETCHED);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getLowStockProducts() {
        log.info("Received request to get low stock products");
        List<ProductResponseDto> response = productService.getLowStockProducts();
        return ApiResponseUtil.success(response, ApiResponseConstant.LOW_STOCK_PRODUCTS_FETCHED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        log.info("Received request to update product with ID: {}", id);
        ProductResponseDto response = productService.updateProduct(id, request);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_UPDATED_SUCCESSFULLY);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Received request to delete product with ID: {}", id);
        productService.deleteProduct(id);
        return ApiResponseUtil.success(null, ApiResponseConstant.PRODUCT_DELETED_SUCCESSFULLY, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        log.info("Received request to update stock for product ID: {} to quantity: {}", id, stock);
        ProductResponseDto response = productService.updateStock(id, stock);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_STOCK_UPDATED_SUCCESSFULLY);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkProductAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("Received request to check availability for product ID: {} with quantity: {}", id, quantity);
        boolean isAvailable = productService.isProductAvailable(id, quantity);
        return ApiResponseUtil.success(isAvailable, ApiResponseConstant.PRODUCT_AVAILABILITY_CHECKED);
    }
}