package com.nahid.order.controller;


import com.nahid.order.dto.CreateOrderRequest;
import com.nahid.order.dto.OrderDto;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.service.OrderService;
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

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        OrderDto orderDto = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID orderId) {
        log.info("Fetching order by ID: {}", orderId);

        OrderDto orderDto = orderService.getOrderById(orderId);

        return ResponseEntity.ok(orderDto);
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Fetching order by order number: {}", orderNumber);

        OrderDto orderDto = orderService.getOrderByOrderNumber(orderNumber);

        return ResponseEntity.ok(orderDto);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Fetching all orders - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDto> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderDto>> getOrdersByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching orders for customer: {}", customerId);

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getOrdersByCustomerId(customerId, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("Fetching orders by status: {}", status);

        List<OrderDto> orders = orderService.getOrdersByStatus(status);

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {

        log.info("Updating order status for ID: {} to status: {}", orderId, status);

        OrderDto orderDto = orderService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(orderDto);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}/count")
    public ResponseEntity<Long> getOrderCountByCustomerAndStatus(
            @PathVariable String customerId,
            @RequestParam OrderStatus status) {

        log.info("Counting orders for customer: {} with status: {}", customerId, status);

        long count = orderService.getOrderCountByCustomerAndStatus(customerId, status);

        return ResponseEntity.ok(count);
    }
}