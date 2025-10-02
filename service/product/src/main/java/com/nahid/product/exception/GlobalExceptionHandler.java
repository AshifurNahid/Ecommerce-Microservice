package com.nahid.product.exception;

import com.nahid.product.dto.response.ApiResponse;
import com.nahid.product.util.helper.ApiResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ApiResponseUtil.failureWithHttpStatus(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(StockUpdateException.class)
    public ResponseEntity<ErrorResponse> handleStockUpdateException(StockUpdateException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.error("Stock update failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error("Stock Update Failed")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.error("Duplicate resource: {}", ex.getMessage());
        return ApiResponseUtil.failureWithHttpStatus(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String key = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(key, errorMessage);
        });

        return ApiResponseUtil.failureWithData(errors, "Invalid input data", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StockUpdateException.class)
    public ResponseEntity<ApiResponse<Object>> handleStockUpdateException(StockUpdateException ex) {
        log.error("Stock update failed: {}", ex.getMessage());
        return ApiResponseUtil.failureWithHttpStatus(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return ApiResponseUtil.failure("An unexpected error occurred");
    }
}
