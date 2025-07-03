package com.nahid.order.client;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

}
