package com.nahid.product.service.Impl;

import com.nahid.product.dto.request.PurchaseProductItemDto;
import com.nahid.product.dto.request.PurchaseProductRequestDto;
import com.nahid.product.dto.response.PurchaseProductItemResultDto;
import com.nahid.product.dto.response.PurchaseProductResponseDto;
import com.nahid.product.entity.Product;
import com.nahid.product.repository.ProductRepository;
import com.nahid.product.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class PurchaseServiceImpl implements PurchaseService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request) {
        log.info("Processing purchase with order reference: {}", request.getOrderReference());

        try {
            // Validate input
            if (request.getItems() == null || request.getItems().isEmpty()) {
                log.warn("Purchase request contains no items for order reference: {}", request.getOrderReference());
                return PurchaseProductResponseDto.builder()
                        .success(false)
                        .message("Purchase request contains no items")
                        .orderReference(request.getOrderReference())
                        .build();
            }

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
                updateInventory(request.getItems(), productsById);
            }

            return PurchaseProductResponseDto.builder()
                    .success(allProductsAvailable)
                    .message(allProductsAvailable ? "All products reserved successfully" : "One or more products are unavailable")
                    .orderReference(request.getOrderReference())
                    .items(itemResults)
                    .build();
        } catch (Exception e) {
            log.error("Error processing purchase for order reference {}: {}", request.getOrderReference(), e.getMessage(), e);
            return PurchaseProductResponseDto.builder()
                    .success(false)
                    .message("Purchase failed: " + e.getMessage())
                    .orderReference(request.getOrderReference())
                    .build();
        }
    }

    private void updateInventory(List<PurchaseProductItemDto> items, Map<Long, Product> productsById) {
        items.forEach(item -> {
            Product product = productsById.get(item.getProductId());
            int newStock = product.getStockQuantity() - item.getQuantity();

            // Prevent negative stock
            if (newStock < 0) {
                log.error("Attempt to set negative stock for product ID {}", product.getId());
                throw new IllegalStateException("Cannot set negative stock for product: " + product.getName());
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);
            log.debug("Updated stock for product ID: {} to quantity: {}", product.getId(), newStock);
        });
    }

    private PurchaseProductItemResultDto buildResultItem(PurchaseProductItemDto item, Product product) {
        if (product == null) {
            log.warn("Product not found with ID: {}", item.getProductId());
            return createUnavailableResult(item, "Product not found");
        }

        boolean isAvailable = product.getIsActive() && product.getStockQuantity() >= item.getQuantity();
        String message = "";
        if (!isAvailable) {
            message = product.getIsActive() ? "Insufficient stock available" : "Product is inactive";
            log.warn("Product {} (ID: {}) is unavailable: {}", product.getName(), product.getId(), message);
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
