package com.nahid.product.controller;

import com.nahid.product.dto.request.CreateProductRequestDto;
import com.nahid.product.dto.request.PurchaseProductRequestDto;
import com.nahid.product.dto.request.UpdateProductRequestDto;
import com.nahid.product.dto.response.ApiResponse;
import com.nahid.product.dto.response.ProductResponseDto;
import com.nahid.product.dto.response.PurchaseProductResponseDto;
import com.nahid.product.service.ProductService;
import com.nahid.product.util.constant.ApiResponseConstant;
import com.nahid.product.util.helper.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        ProductResponseDto response = productService.createProduct(request);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_CREATED_SUCCESSFULLY, HttpStatus.CREATED);
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchaseProductResponseDto>> purchaseProduct(@Valid @RequestBody PurchaseProductRequestDto request) {
        PurchaseProductResponseDto response = productService.processPurchase(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.CONFLICT;
        if (response.isSuccess()) {
            return ApiResponseUtil.success(response, response.getMessage(), status);
        }
        return ApiResponseUtil.failureWithData(response, response.getMessage(), status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductById(@PathVariable Long id) {
        ProductResponseDto response = productService.getProductById(id);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProductBySku(@PathVariable String sku) {
        ProductResponseDto response = productService.getProductBySku(sku);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_FETCHED_SUCCESSFULLY);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> response = productService.getAllProducts(pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCTS_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getActiveProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> response = productService.getActiveProducts(pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.ACTIVE_PRODUCTS_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
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
        Page<ProductResponseDto> response = productService.searchProducts(name, brand, minPrice, maxPrice, categoryId, pageable);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCTS_SEARCHED_SUCCESSFULLY);
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getFeaturedProducts() {
        List<ProductResponseDto> response = productService.getFeaturedProducts();
        return ApiResponseUtil.success(response, ApiResponseConstant.FEATURED_PRODUCTS_FETCHED);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getLowStockProducts() {
        List<ProductResponseDto> response = productService.getLowStockProducts();
        return ApiResponseUtil.success(response, ApiResponseConstant.LOW_STOCK_PRODUCTS_FETCHED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        ProductResponseDto response = productService.updateProduct(id, request);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_UPDATED_SUCCESSFULLY);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponseUtil.success(null, ApiResponseConstant.PRODUCT_DELETED_SUCCESSFULLY, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        ProductResponseDto response = productService.updateStock(id, stock);
        return ApiResponseUtil.success(response, ApiResponseConstant.PRODUCT_STOCK_UPDATED_SUCCESSFULLY);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkProductAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        boolean isAvailable = productService.isProductAvailable(id, quantity);
        return ApiResponseUtil.success(isAvailable, ApiResponseConstant.PRODUCT_AVAILABILITY_CHECKED);
    }
}