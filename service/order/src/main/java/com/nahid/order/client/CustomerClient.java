package com.nahid.order.client;

import com.nahid.order.dto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.Optional;
import java.util.UUID;

@FeignClient(
        name = "customer-service",
        url = "${application.config.customer-service.url}",
        fallback = CustomerFeignClientFallback.class)
public interface CustomerClient {

    @GetMapping("/{customerId}")
    Optional<CustomerResponseDto> getCustomerById(@PathVariable String customerId);

}
