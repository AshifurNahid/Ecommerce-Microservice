package com.nahid.product.service;

import com.nahid.product.dto.ProductResponseDto;

public interface InventoryService {

    ProductResponseDto updateStock(Long id, Integer newStock);

    boolean isProductAvailable(Long id, Integer requiredQuantity);
}

