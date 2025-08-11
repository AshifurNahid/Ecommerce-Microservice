package com.nahid.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T>{
    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private Instant timestamp;
}
