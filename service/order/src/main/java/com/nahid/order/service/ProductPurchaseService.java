package com.nahid.order.service;

import com.nahid.order.dto.CreateOrderRequest;
import com.nahid.order.dto.PurchaseProductResponseDto;

public interface ProductPurchaseService {

    PurchaseProductResponseDto purchaseProducts(CreateOrderRequest request);
    String formatPurchaseError(PurchaseProductResponseDto response);
}

