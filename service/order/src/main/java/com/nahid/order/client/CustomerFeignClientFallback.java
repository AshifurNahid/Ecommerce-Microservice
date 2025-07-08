package com.nahid.order.client;


import com.nahid.order.dto.CustomerResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class CustomerFeignClientFallback implements CustomerClient {

    @Override
    public Optional<CustomerResponseDto> getCustomerById(String customerId) {
        log.warn("Fallback: Unable to fetch customer with ID: {}", customerId);
        return Optional.empty();
    }

}