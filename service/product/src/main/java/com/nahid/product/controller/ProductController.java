package com.nahid.product.controller;

import com.nahid.product.dto.*;
import com.nahid.product.service.ProductService;
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
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody CreateProductRequestDto request) {
        log.info("Received request to create product with SKU: {}", request.getSku());
        ProductResponseDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/purchase")
    public ResponseEntity<PurchaseProductResponseDto> purchaseProduct(@Valid @RequestBody PurchaseProductRequestDto request) {
        log.info("Received purchase request for order reference: {}", request.getOrderReference());
        PurchaseProductResponseDto response = productService.processPurchase(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        log.info("Received request to get product with ID: {}", id);
        ProductResponseDto response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        log.info("Received request to get product with SKU: {}", sku);
        ProductResponseDto response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponseDto>> getActiveProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponseDto> response = productService.getActiveProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get products by category ID: {} with pagination: {}", categoryId, pageable);
        Page<ProductResponseDto> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to search products with filters");
        Page<ProductResponseDto> response = productService.searchProducts(name, brand, minPrice, maxPrice, categoryId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponseDto>> getFeaturedProducts() {
        log.info("Received request to get featured products");
        List<ProductResponseDto> response = productService.getFeaturedProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponseDto>> getLowStockProducts() {
        log.info("Received request to get low stock products");
        List<ProductResponseDto> response = productService.getLowStockProducts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        log.info("Received request to update product with ID: {}", id);
        ProductResponseDto response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Received request to delete product with ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDto> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        log.info("Received request to update stock for product ID: {} to quantity: {}", id, stock);
        ProductResponseDto response = productService.updateStock(id, stock);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkProductAvailability(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("Received request to check availability for product ID: {} with quantity: {}", id, quantity);
        boolean isAvailable = productService.isProductAvailable(id, quantity);
        return ResponseEntity.ok(isAvailable);
    }
}