package com.nahid.order.client;

import com.nahid.order.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(
        name = "user-service",
        url = "${application.config.user-service.url}",
        fallback = UserFeignClientFallback.class)
public interface UserClient {

    @GetMapping("/api/users/public/{userId}")
    Optional<UserResponseDto> getUserById(@PathVariable Long userId);

}
