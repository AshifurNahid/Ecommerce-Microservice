package com.nahid.product.service;

import com.nahid.product.dto.PurchaseProductRequestDto;
import com.nahid.product.dto.PurchaseProductResponseDto;


public interface PurchaseService {

    PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request);

}

