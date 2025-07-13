package com.nahid.order.service.impl;

import com.nahid.order.client.CustomerClient;
import com.nahid.order.dto.*;
import com.nahid.order.entity.Order;
import com.nahid.order.entity.OrderItem;
import com.nahid.order.enums.CustomerStatus;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.exception.OrderNotFoundException;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.mapper.OrderMapper;
import com.nahid.order.repository.OrderRepository;
import com.nahid.order.service.OrderService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerClient customerClient;

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        try {
            validateCustomerForOrder(request.getCustomerId());

            PurchaseProductResponseDto purchaseResponse = purchaseProducts(request);




            Order order = orderMapper.toEntity(request);
            order.setOrderNumber(generateOrderNumber());
            order.setStatus(OrderStatus.PENDING);

            List<OrderItem> orderItems = request.getOrderItems().stream()
                    .map(itemRequest -> {
                        OrderItem item = orderMapper.toEntity(itemRequest);
                        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        return item;
                    })
                    .toList();

            BigDecimal totalAmount = orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(totalAmount);

            //orderItems.forEach(order::addOrderItem);
            for (OrderItem item : orderItems) {
                order.addOrderItem(item);
            }

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully with ID: {} and number: {}",
                    savedOrder.getOrderId(), savedOrder.getOrderNumber());

            return orderMapper.toDto(savedOrder);

        } catch (Exception e) {
            log.error("Error creating order for customer: {}", request.getCustomerId(), e);
            throw new OrderProcessingException("Failed to create order", e);
        }
    }

    private PurchaseProductResponseDto purchaseProducts(CreateOrderRequest request) {
        PurchaseProductRequestDto purchaseRequest = PurchaseProductRequestDto.builder()
                .items(request.getOrderItems().stream()
                        .map(item -> PurchaseProductItemDto.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private void validateCustomerForOrder(@NotNull(message = "Customer ID is required") String customerId) {


        Optional<CustomerResponseDto> customerResponseDto = customerClient.getCustomerById(customerId);
        if (customerResponseDto.isEmpty()) {
            log.error("Customer not found with ID: {}", customerId);
            throw new OrderProcessingException("Customer not found with ID: " + customerId);
        }
        if (CustomerStatus.SUSPENDED == customerResponseDto.get().getStatus()) {
            log.error("Customer with ID: {} is Suspended", customerId);
            throw new OrderProcessingException("Customer is Suspended");
        }
        log.debug("Customer with ID: {} is valid and active", customerId);

        if (CustomerStatus.INACTIVE == customerResponseDto.get().getStatus() ) {
            log.error("Customer with ID: {} is Inactive", customerId);
            throw new OrderProcessingException("Customer is Inactive");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {
        log.debug("Fetching order by ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByCustomerId(UUID customerId, Pageable pageable) {
        log.debug("Fetching orders for customer: {}", customerId);

        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination");

        return orderRepository.findAll(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus status) {
        log.info("Updating order status for ID: {} to status: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for ID: {}", orderId);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order cancelled successfully with ID: {}", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders by status: {}", status);

        return orderRepository.findByStatusOrderByCreatedAtDesc(status, Pageable.unpaged())
                .getContent()
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getOrderCountByCustomerAndStatus(UUID customerId, OrderStatus status) {
        log.debug("Counting orders for customer: {} with status: {}", customerId, status);

        return orderRepository.countByCustomerIdAndStatus(customerId, status);
    }

    private String generateOrderNumber() {
        String prefix = "ORD";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.DELIVERED) {
            throw new OrderProcessingException("Cannot deliver an order that is still pending");
        }
        if (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Cannot cancel an order that is still pending");
        }
        if (currentStatus == OrderStatus.PENDING) {
            throw new OrderProcessingException("Cannot change status of pending order to " + newStatus);
        }
        if (currentStatus == OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED) {
            throw new OrderProcessingException("Cannot change status of shipped order to " + newStatus);
        }
        if (currentStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Cannot change status of cancelled order");
        }
        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.REFUNDED) {
            throw new OrderProcessingException("Can only refund delivered orders");
        }
    }
}