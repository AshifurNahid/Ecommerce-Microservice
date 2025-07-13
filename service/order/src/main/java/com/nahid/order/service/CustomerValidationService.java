package com.nahid.order.service;

import jakarta.validation.constraints.NotNull;

public interface CustomerValidationService {

    void validateCustomerForOrder(@NotNull(message = "Customer ID is required") String customerId);
}

