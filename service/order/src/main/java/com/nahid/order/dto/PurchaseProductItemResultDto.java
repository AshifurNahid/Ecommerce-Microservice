package com.nahid.order.dto;

import lombok.Getter;


import java.math.BigDecimal;

@Getter
public class PurchaseProductItemResultDto {
    private Long productId;
    private String productName;
    private String sku;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private BigDecimal price;
    private boolean available;
    private String message;
}
