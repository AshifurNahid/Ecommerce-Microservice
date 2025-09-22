package com.nahid.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseProductResponseDto {
    private boolean success;
    private String message;
    private String orderReference;
    private List<PurchaseProductItemResultDto> items;

}
