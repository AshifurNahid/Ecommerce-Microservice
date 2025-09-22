package com.nahid.order.client;


import com.nahid.order.dto.response.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class UserFeignClientFallback implements UserClient {

    @Override
    public Optional<UserResponseDto> getUserById(Long userId) {
        log.error("Fallback triggered for getUserById with userId: {}", userId);
        return Optional.empty();
    }

}