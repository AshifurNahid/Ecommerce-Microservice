package com.nahid.order.controller;


import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.OrderDto;
import com.nahid.order.dto.response.ApiResponse;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.service.OrderService;
import com.nahid.order.util.helper.ApiResponseUtil;
import com.nahid.order.util.constant.ApiResponseConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Tag(
        name = "Order Management",
        description = "Order Management API"
)
@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Create a new order")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDto orderDto = orderService.createOrder(request);
        return ApiResponseUtil.success(orderDto, ApiResponseConstant.ORDER_CREATED_SUCCESSFULLY, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable UUID orderId) {
        OrderDto orderDto = orderService.getOrderById(orderId);
        return ApiResponseUtil.success(orderDto, ApiResponseConstant.ORDER_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDto orderDto = orderService.getOrderByOrderNumber(orderNumber);
        return ApiResponseUtil.success(orderDto, ApiResponseConstant.ORDER_FETCHED_SUCCESSFULLY);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDto> orders = orderService.getAllOrders(pageable);

        return ApiResponseUtil.success(orders, ApiResponseConstant.ORDERS_FETCHED_SUCCESSFULLY);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getOrdersByUserId(userId, pageable);

        return ApiResponseUtil.success(orders, ApiResponseConstant.ORDERS_BY_USER_FETCHED);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        String message = String.format(ApiResponseConstant.ORDERS_BY_STATUS_FETCHED, status);
        return ApiResponseUtil.success(orders, message);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {

        OrderDto orderDto = orderService.updateOrderStatus(orderId, status);
        return ApiResponseUtil.success(orderDto, ApiResponseConstant.ORDER_STATUS_UPDATED);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrder(orderId);
        return ApiResponseUtil.success(null, ApiResponseConstant.ORDER_CANCELLED_SUCCESSFULLY, HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getOrderCountByUserAndStatus(
            @PathVariable Long userId,
            @RequestParam OrderStatus status) {
        long count = orderService.getOrderCountByUserAndStatus(userId, status);
        return ApiResponseUtil.success(count, ApiResponseConstant.ORDER_COUNT_FETCHED);
    }
}