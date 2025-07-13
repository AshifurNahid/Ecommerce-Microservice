package com.nahid.order.client;


import com.nahid.order.dto.PurchaseProductRequestDto;
import com.nahid.order.dto.PurchaseProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class ProductClient {

    @Value("${application.config.product-service.url}")
    private String baseUrl ;

    private final RestTemplate restTemplate;

    public String getProductById(String productId) {
        String url = baseUrl + "/api/v1/products/" + productId;
        return restTemplate.getForObject(url, String.class);
    }


    public PurchaseProductResponseDto purchaseProduct(PurchaseProductRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<PurchaseProductRequestDto> entity = new HttpEntity<>(request, headers);
        ParameterizedTypeReference<PurchaseProductResponseDto> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity< PurchaseProductResponseDto> responseEntity = restTemplate.exchange(
                baseUrl + "/api/v1/products/purchase",
                HttpMethod.POST,
                entity,
                responseType
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to purchase product: " + responseEntity.getStatusCode());
        }
    }

}
