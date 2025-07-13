package com.nahid.order.service.impl;

import com.nahid.order.client.CustomerClient;
import com.nahid.order.dto.CustomerResponseDto;
import com.nahid.order.enums.CustomerStatus;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.service.CustomerValidationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerValidationServiceImpl implements CustomerValidationService {

    private final CustomerClient customerClient;

    @Override
    public void validateCustomerForOrder(@NotNull(message = "Customer ID is required") String customerId) {
        Optional<CustomerResponseDto> customerResponseDto = customerClient.getCustomerById(customerId);

        if (customerResponseDto.isEmpty()) {
            log.error("Customer not found with ID: {}", customerId);
            throw new OrderProcessingException("Customer not found with ID: " + customerId);
        }

        CustomerResponseDto customer = customerResponseDto.get();

        if (CustomerStatus.SUSPENDED == customer.getStatus()) {
            log.error("Customer with ID: {} is Suspended", customerId);
            throw new OrderProcessingException("Customer is Suspended");
        }

        if (CustomerStatus.INACTIVE == customer.getStatus()) {
            log.error("Customer with ID: {} is Inactive", customerId);
            throw new OrderProcessingException("Customer is Inactive");
        }

        log.debug("Customer with ID: {} is valid and active", customerId);
    }
}

