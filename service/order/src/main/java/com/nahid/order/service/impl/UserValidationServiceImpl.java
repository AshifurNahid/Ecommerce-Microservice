package com.nahid.order.service.impl;

import com.nahid.order.client.UserClient;
import com.nahid.order.dto.response.UserResponseDto;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.service.UserValidationService;
import com.nahid.order.util.constant.ExceptionMessageConstant;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationServiceImpl implements UserValidationService {

    private final UserClient userClient;

    @Override
    public void validateUserForOrder(@NotNull(message = "User ID is required") Long userId) {
        log.info(LoggingConstant.USER_VALIDATION_INITIATED, userId);

        try {
            Optional<UserResponseDto> userResponseDto = userClient.getUserById(userId);

            if (userResponseDto.isEmpty()) {
                log.error(LoggingConstant.USER_VALIDATION_FAILED, userId);
                throw new OrderProcessingException(String.format(ExceptionMessageConstant.USER_NOT_FOUND, userId));
            }

            UserResponseDto user = userResponseDto.get();

            validateUserStatus(user, userId);

            log.debug(LoggingConstant.USER_VALIDATION_SUCCESSFUL, userId);

        } catch (OrderProcessingException e) {
            log.error(LoggingConstant.USER_VALIDATION_FAILED, userId);
            throw e;
        } catch (Exception e) {
            log.error(LoggingConstant.USER_VALIDATION_FAILED, userId, e);
            throw new OrderProcessingException(String.format(ExceptionMessageConstant.USER_VALIDATION_FAILED, userId), e);
        }
    }

    private void validateUserStatus(UserResponseDto user, Long userId) {
        switch (user.getStatus()) {
            case SUSPENDED:
                log.error("User with ID: {} is Suspended", userId);
                throw new OrderProcessingException(ExceptionMessageConstant.USER_SUSPENDED);

            case INACTIVE:
                log.error("User with ID: {} is Inactive", userId);
                throw new OrderProcessingException(ExceptionMessageConstant.USER_INACTIVE);

            case BLOCKED:
                log.error("User with ID: {} is Blocked", userId);
                throw new OrderProcessingException(ExceptionMessageConstant.USER_BLOCKED);

            case ACTIVE:
                log.debug("User with ID: {} is active and valid", userId);
                break;

            default:
                log.warn("User with ID: {} has unknown status: {}", userId, user.getStatus());
                throw new OrderProcessingException(String.format(ExceptionMessageConstant.USER_VALIDATION_FAILED, userId));
        }
    }
}
