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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDto createProduct(CreateProductRequestDto request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            log.error("Product with SKU {} already exists", request.getSku());
            throw new DuplicateResourceException("Product with SKU " + request.getSku() + " already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Category with ID {} not found", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId());
                });

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setIsActive(true); 
        product.setImageUrl(request.getImageUrl()); 
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
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
        log.debug("Searching products with filters - name: {}, brand: {}, minPrice: {}, maxPrice: {}, categoryId: {}",
                name, brand, minPrice, maxPrice, categoryId);

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
    public ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request) {
        log.info("Updating product with ID: {}", id);

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
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            log.error("Product with ID {} not found", id);
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with ID: {}", id);
    }

    @Override
    public ProductResponseDto updateStock(Long id, Integer newStock) {
        log.info("Updating stock for product ID: {} to quantity: {}", id, newStock);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with ID: " + id);
                });

        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated successfully for product ID: {}", id);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Long id, Integer requiredQuantity) {
        log.debug("Checking availability for product ID: {} with required quantity: {}", id, requiredQuantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product not found with ID: " + id);
                });

        boolean isAvailable = product.getIsActive() && product.getStockQuantity() >= requiredQuantity;
        log.debug("Product availability check result: {}", isAvailable);
        return isAvailable;
    }


    @Override
    public PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request) {
        List<Long> productIds = request.getItems().stream()
                .map(PurchaseProductItemDto::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productsById = new HashMap<>();
        for (Product product : products) {
            productsById.put(product.getId(), product);
        }
        List<PurchaseProductItemResultDto> itemResults = new ArrayList<>();
        boolean allProductsAvailable = true;

        for (PurchaseProductItemDto item : request.getItems()) {
            Product product = productsById.get(item.getProductId());
            PurchaseProductItemResultDto result = buildResultItem(item, product);
            itemResults.add(result);
            if (!result.isAvailable()) {
                allProductsAvailable = false;
            }
        }

        if (allProductsAvailable) {
            request.getItems().forEach(item -> {
                Product product = productsById.get(item.getProductId());
                int newStock = product.getStockQuantity() - item.getQuantity();
                product.setStockQuantity(newStock);
                productRepository.save(product);
            });
        }

        return PurchaseProductResponseDto.builder()
                .success(allProductsAvailable)
                .message(allProductsAvailable ? "All products reserved successfully" : "One or more products are unavailable")
                .orderReference(request.getOrderReference())
                .items(itemResults)
                .build();
    }

    private PurchaseProductItemResultDto buildResultItem(PurchaseProductItemDto item, Product product) {
        if (product == null) {
            return createUnavailableResult(item, "Product not found");
        }

        boolean isAvailable = product.getIsActive() && product.getStockQuantity() >= item.getQuantity();
        String message = "";
        if (!isAvailable) {
            message = product.getIsActive() ? "Insufficient stock available" : "Product is inactive";
        }

        return PurchaseProductItemResultDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .requestedQuantity(item.getQuantity())
                .availableQuantity(product.getStockQuantity())
                .available(isAvailable)
                .message(message)
                .build();
    }

    private PurchaseProductItemResultDto createUnavailableResult(PurchaseProductItemDto item, String message) {
        return PurchaseProductItemResultDto.builder()
                .productId(item.getProductId())
                .requestedQuantity(item.getQuantity())
                .available(false)
                .message(message)
                .build();
    }

}
