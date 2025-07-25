package com.nahid.order.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseProductRequestDto {

    private String orderReference;
    private List<PurchaseProductItemDto> items;
}