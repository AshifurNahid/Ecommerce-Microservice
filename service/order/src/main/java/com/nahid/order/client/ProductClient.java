package com.nahid.order.client;

import com.nahid.order.dto.request.PurchaseProductRequestDto;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "${application.config.product-service.url}")
public interface ProductClient {

    @PostMapping(value = "/api/v1/products/inventory/reservations", consumes = MediaType.APPLICATION_JSON_VALUE)
    PurchaseProductResponseDto reserveInventory(@RequestBody PurchaseProductRequestDto request);

    @PostMapping("/api/v1/products/inventory/reservations/{orderReference}/confirm")
    void confirmReservation(@PathVariable("orderReference") String orderReference);

    @PostMapping("/api/v1/products/inventory/reservations/{orderReference}/release")
    void releaseReservation(@PathVariable("orderReference") String orderReference);
}
