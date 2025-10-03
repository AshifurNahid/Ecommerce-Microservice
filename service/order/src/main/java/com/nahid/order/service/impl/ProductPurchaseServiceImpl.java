package com.nahid.order.service.impl;

import com.nahid.order.client.ProductClient;
import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.PurchaseProductItemDto;
import com.nahid.order.dto.request.PurchaseProductRequestDto;
import com.nahid.order.dto.response.ApiResponse;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.service.ProductPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductPurchaseServiceImpl implements ProductPurchaseService {

    private final ProductClient productClient;

    @Override
    public PurchaseProductResponseDto reserveProducts(CreateOrderRequest request, String orderReference) {

        PurchaseProductRequestDto purchaseRequest = PurchaseProductRequestDto.builder()
                .orderReference(orderReference)
                .items(request.getOrderItems().stream()
                        .map(item -> PurchaseProductItemDto.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();

        ResponseEntity<ApiResponse<PurchaseProductResponseDto>> responseEntity = productClient.reserveInventory(purchaseRequest);
        ApiResponse<PurchaseProductResponseDto> apiResponse =
                responseEntity != null ? responseEntity.getBody() : null;

        if (apiResponse == null || apiResponse.getData() == null) {
            throw new OrderProcessingException(" failed to reserve inventory with orderReference " + orderReference);
        }

        return apiResponse.getData();
    }

    @Override
    public void confirmReservation(String orderReference) {
        productClient.confirmReservation(orderReference);
    }

    @Override
    public void releaseReservation(String orderReference) {
        productClient.releaseReservation(orderReference);
    }

    @Override
    public String formatReservationError(PurchaseProductResponseDto response) {
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

