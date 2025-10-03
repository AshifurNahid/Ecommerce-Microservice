package com.nahid.order.client;


import com.nahid.order.dto.request.PurchaseProductRequestDto;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class ProductClient {

    @Value("${application.config.product-service.url}")
    private String baseUrl ;

    private final RestTemplate restTemplate;

    public PurchaseProductResponseDto reserveInventory(PurchaseProductRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<PurchaseProductRequestDto> entity = new HttpEntity<>(request, headers);
        ResponseEntity< PurchaseProductResponseDto> responseEntity = restTemplate.exchange(
                baseUrl + "/api/v1/products/inventory/reservations",
                HttpMethod.POST,
                entity,
                PurchaseProductResponseDto.class
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to reserve inventory: " + responseEntity.getStatusCode());
        }
    }

    public void confirmReservation(String orderReference) {
        restTemplate.postForEntity(
                baseUrl + "/api/v1/products/inventory/reservations/" + orderReference + "/confirm",
                null,
                Void.class
        );
    }

    public void releaseReservation(String orderReference) {
        restTemplate.postForEntity(
                baseUrl + "/api/v1/products/inventory/reservations/" + orderReference + "/release",
                null,
                Void.class
        );
    }

}
