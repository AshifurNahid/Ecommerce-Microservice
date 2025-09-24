package com.nahid.product.service.Impl;

import com.nahid.product.dto.*;
import com.nahid.product.entity.Category;
import com.nahid.product.entity.Product;
import com.nahid.product.exception.DuplicateResourceException;
import com.nahid.product.exception.ResourceNotFoundException;
import com.nahid.product.mapper.ProductMapper;
import com.nahid.product.repository.CategoryRepository;
import com.nahid.product.repository.ProductRepository;
import com.nahid.product.service.ProductService;
import com.nahid.product.service.InventoryService;
import com.nahid.product.service.PurchaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main product service that delegates to specialized services
 * Follows SOLID principles by:
 * - Acting as a facade to coordinate between specialized services
 * - Delegating specific responsibilities to appropriate services
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;
    private final PurchaseService purchaseService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProductResponseDto createProduct(CreateProductRequestDto request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        try {
            if (productRepository.existsBySku(request.getSku())) {
                log.error("Product with SKU {} already exists", request.getSku());
                throw new DuplicateResourceException("Product with SKU " + request.getSku() + " already exists");
            }

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        //log.error("Category with ID {} not found", request.getCategoryId());
                        return new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
                    });

            Product product = productMapper.toEntity(request);
            product.setCategory(category);
            product.setIsActive(true);
            product.setImageUrl(request.getImageUrl());

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully with ID: {}", savedProduct.getId());

            return productMapper.toResponse(savedProduct);
        } catch (DuplicateResourceException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating product with SKU: {}", request.getSku(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with ID: " + id);
                });

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.error("Product with SKU {} not found", sku);
                    return new ResourceNotFoundException("Product not found with SKU: " + sku);
                });

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);

        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getActiveProducts(Pageable pageable) {
        log.debug("Fetching active products with pagination: {}", pageable);

        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products by category ID: {} with pagination: {}", categoryId, pageable);

        if (!categoryRepository.existsById(categoryId)) {
            log.error("Category with ID {} not found", categoryId);
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }

        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String name, String brand, BigDecimal minPrice,
                                                BigDecimal maxPrice, Long categoryId, Pageable pageable) {

        Page<Product> products = productRepository.findProductsWithFilters(
                name, brand, minPrice, maxPrice, categoryId, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getFeaturedProducts() {
        log.debug("Fetching featured products");

        List<Product> products = productRepository.findByIsFeaturedTrue();
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getLowStockProducts() {
        log.debug("Fetching low stock products");

        List<Product> products = productRepository.findLowStockProducts();
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request) {
        log.info("Updating product with ID: {}", id);

        try {
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Product with ID {} not found", id);
                        return new ResourceNotFoundException("Product not found with ID: " + id);
                    });

            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> {
                            log.error("Category with ID {} not found", request.getCategoryId());
                            return new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
                        });
                existingProduct.setCategory(category);
            }

            productMapper.updateProductFromRequest(request, existingProduct);

            Product updatedProduct = productRepository.save(existingProduct);
            log.info("Product updated successfully with ID: {}", updatedProduct.getId());

            return productMapper.toResponse(updatedProduct);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating product with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        try {
            if (!productRepository.existsById(id)) {
                log.error("Product with ID {} not found", id);
                throw new ResourceNotFoundException("Product not found with ID: " + id);
            }

            productRepository.deleteById(id);
            log.info("Product deleted successfully with ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting product with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ProductResponseDto updateStock(Long id, Integer newStock) {
        log.info("Delegating stock update to InventoryService for product ID: {}", id);
        try {
            return inventoryService.updateStock(id, newStock);
        } catch (Exception e) {
            log.error("Error updating stock for product ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long id, Integer requiredQuantity) {
        log.debug("Delegating availability check to InventoryService for product ID: {}", id);
        return inventoryService.isProductAvailable(id, requiredQuantity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request) {
        log.info("Delegating purchase processing to PurchaseService");
        try {
            return purchaseService.processPurchase(request);
        } catch (Exception e) {
            log.error("Error processing purchase: {}", e.getMessage(), e);
            throw e;
        }
    }

}
