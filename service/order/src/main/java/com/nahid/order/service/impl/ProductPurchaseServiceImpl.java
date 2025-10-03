package com.nahid.order.service.impl;

import com.nahid.order.client.ProductClient;
import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.PurchaseProductItemDto;
import com.nahid.order.dto.request.PurchaseProductRequestDto;
import com.nahid.order.dto.response.ApiResponse;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
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
        List<PurchaseProductItemDto> items = request.getOrderItems().stream()
                .map(item -> PurchaseProductItemDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        PurchaseProductRequestDto purchaseRequest = PurchaseProductRequestDto.builder()
                .orderReference(orderReference)
                .items(items)
                .build();

        ResponseEntity<ApiResponse<PurchaseProductResponseDto>> responseEntity =
                productClient.reserveInventory(purchaseRequest);
        ApiResponse<PurchaseProductResponseDto> apiResponse =
                responseEntity != null ? responseEntity.getBody() : null;

        return mapReservationResponse(apiResponse, orderReference);
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

    private PurchaseProductResponseDto mapReservationResponse(ApiResponse<PurchaseProductResponseDto> apiResponse,
                                                              String orderReference) {
        if (apiResponse == null) {
            log.error("Received null response from product service for orderReference {}", orderReference);
            return buildFailureResponse("Failed to reserve inventory: no response from product service", orderReference);
        }

        PurchaseProductResponseDto response = apiResponse.getData();
        if (response == null) {
            log.error("Product service returned empty reservation data for orderReference {}", orderReference);
            return buildFailureResponse(apiResponse.getMessage(), orderReference);
        }

        response.setSuccess(apiResponse.isSuccess());
        if (response.getMessage() == null || response.getMessage().isBlank()) {
            response.setMessage(apiResponse.getMessage());
        }
        if (response.getOrderReference() == null) {
            response.setOrderReference(orderReference);
        }
        if (response.getItems() == null) {
            response.setItems(Collections.emptyList());
        }
        return response;
    }

    private PurchaseProductResponseDto buildFailureResponse(String message, String orderReference) {
        return PurchaseProductResponseDto.builder()
                .success(false)
                .message(message != null ? message : "Failed to reserve inventory")
                .orderReference(orderReference)
                .items(Collections.emptyList())
                .build();
    }
}

