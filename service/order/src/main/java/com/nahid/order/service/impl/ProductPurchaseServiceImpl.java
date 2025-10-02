package com.nahid.order.service.impl;

import com.nahid.order.client.ProductClient;
import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.PurchaseProductItemDto;
import com.nahid.order.dto.request.PurchaseProductRequestDto;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import com.nahid.order.service.ProductPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPurchaseServiceImpl implements ProductPurchaseService {

    private final ProductClient productClient;

    @Override
    public PurchaseProductResponseDto purchaseProducts(CreateOrderRequest request, String orderReference) {
        List<PurchaseProductItemDto> items = request.getOrderItems().stream()
                .map(item -> PurchaseProductItemDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        PurchaseProductRequestDto purchaseRequest = PurchaseProductRequestDto.builder()
                .orderReference(orderReference)
                .items(items)
                .build();

        log.info("Calling product service to purchase products for order reference: {}", orderReference);
        return productClient.purchaseProduct(purchaseRequest);
    }

    @Override
    public String formatPurchaseError(PurchaseProductResponseDto response) {
        StringBuilder errorBuilder = new StringBuilder();

        if (response != null) {
            errorBuilder.append(response.getMessage()).append(": ");
            if (response.getItems() != null) {
                List<String> itemErrors = response.getItems().stream()
                        .filter(item -> !item.isAvailable())
                        .map(item -> String.format("Product %s (ID: %d, SKU: %s) - %s (Requested: %d, Available: %d)",
                                item.getProductName(),
                                item.getProductId(),
                                item.getSku(),
                                item.getMessage(),
                                item.getRequestedQuantity(),
                                item.getAvailableQuantity()))
                        .toList();

                errorBuilder.append(String.join("; ", itemErrors));
            }
        } else {
            errorBuilder.append("Failed to get response from product service");
        }

        return errorBuilder.toString();
    }
}

