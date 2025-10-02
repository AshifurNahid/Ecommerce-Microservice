package com.nahid.product.service;

import com.nahid.product.dto.request.PurchaseProductRequestDto;
import com.nahid.product.dto.response.PurchaseProductResponseDto;


public interface PurchaseService {

    PurchaseProductResponseDto processPurchase(PurchaseProductRequestDto request);

}

