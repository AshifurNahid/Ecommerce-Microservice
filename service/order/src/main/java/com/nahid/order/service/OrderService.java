package com.nahid.order.service;

import com.nahid.order.dto.CreateOrderRequest;
import com.nahid.order.dto.OrderDto;
import com.nahid.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDto createOrder(CreateOrderRequest request);

    OrderDto getOrderById(UUID orderId);

    OrderDto getOrderByOrderNumber(String orderNumber);

    Page<OrderDto> getOrdersByCustomerId(UUID customerId, Pageable pageable);

    Page<OrderDto> getAllOrders(Pageable pageable);

    OrderDto updateOrderStatus(UUID orderId, OrderStatus status);

    void cancelOrder(UUID orderId);

    List<OrderDto> getOrdersByStatus(OrderStatus status);

    long getOrderCountByCustomerAndStatus(UUID customerId, OrderStatus status);
}