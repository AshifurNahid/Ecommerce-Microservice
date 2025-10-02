package com.nahid.product.service.Impl;

import com.nahid.product.dto.ProductResponseDto;
import com.nahid.product.entity.Product;
import com.nahid.product.exception.ResourceNotFoundException;
import com.nahid.product.exception.StockUpdateException;
import com.nahid.product.mapper.ProductMapper;
import com.nahid.product.repository.ProductRepository;
import com.nahid.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public ProductResponseDto updateStock(Long id, Integer newStock) {

        try {
            if (newStock < 0) {
                throw new StockUpdateException("Stock quantity cannot be negative");
            }
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
            product.setStockQuantity(newStock);
            Product updatedProduct = productRepository.save(product);
            return productMapper.toResponse(updatedProduct);
        } catch (ResourceNotFoundException | StockUpdateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating stock for product ID: {}", id, e);
            throw new StockUpdateException("Failed to update stock: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean isProductAvailable(Long id, Integer requiredQuantity) {
        log.debug("Checking availability for product ID: {} with required quantity: {}", id, requiredQuantity);

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Product with ID {} not found", id);
                        return new ResourceNotFoundException("Product not found with ID: " + id);
                    });

            boolean isAvailable = product.getIsActive() && product.getStockQuantity() >= requiredQuantity;
            log.debug("Product availability check result: {}", isAvailable);
            return isAvailable;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking product availability: {}", e.getMessage(), e);
            throw e;
        }
    }
}
