package com.nahid.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Currency is required")
    private String currency;

    @Valid
    @NotNull(message = "Shipping address is required")
    private ShippingAddressDto shippingAddress;

    @Valid
    @NotEmpty(message = "Order items cannot be empty")
    private List<CreateOrderItemRequest> orderItems;
}