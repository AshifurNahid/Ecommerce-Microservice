package com.nahid.order.service;

import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.response.PurchaseProductResponseDto;

public interface ProductPurchaseService {

    PurchaseProductResponseDto purchaseProducts(CreateOrderRequest request, String orderReference);
    String formatPurchaseError(PurchaseProductResponseDto response);
}

