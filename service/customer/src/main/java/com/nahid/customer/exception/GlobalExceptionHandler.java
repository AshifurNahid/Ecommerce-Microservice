package com.nahid.customer.exception;

import com.nahid.customer.dto.ApiResponse;
import com.nahid.customer.util.contant.ExceptionMessageConstant;
import com.nahid.customer.util.helper.ApiResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomerNotFound(CustomerNotFoundException e) {
        log.error("Customer not found: {}", e.getMessage());
        return ApiResponseUtil.failureWithHttpStatus(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomerAlreadyExists(CustomerAlreadyExistsException e) {
        log.error("Customer already exists: {}", e.getMessage());
        return ApiResponseUtil.failureWithHttpStatus(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException e) {
        log.error("Validation error occurred");
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ApiResponseUtil.failureWithHttpStatus("Input validation failed", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return ApiResponseUtil.failure("An unexpected error occurred");
    }
}