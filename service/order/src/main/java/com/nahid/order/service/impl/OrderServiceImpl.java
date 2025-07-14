package com.nahid.order.service.impl;

import com.nahid.order.dto.*;
import com.nahid.order.entity.Order;
import com.nahid.order.entity.OrderItem;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.exception.OrderNotFoundException;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.mapper.OrderMapper;
import com.nahid.order.producer.OrderEventPublisher;
import com.nahid.order.repository.OrderRepository;
import com.nahid.order.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerValidationService customerValidationService;
    private final ProductPurchaseService productPurchaseService;
    private final OrderStatusService orderStatusService;
    private final OrderNumberService orderNumberService;
    private  final OrderEventPublisher orderEventPublisher;

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        try {
            customerValidationService.validateCustomerForOrder(request.getCustomerId());

            PurchaseProductResponseDto purchaseResponse = productPurchaseService.purchaseProducts(request);

            if (purchaseResponse == null || !purchaseResponse.isSuccess()) {
                String errorMessage = productPurchaseService.formatPurchaseError(purchaseResponse);
                log.error("Product purchase failed: {}", errorMessage);
                throw new OrderProcessingException("Cannot create order: " + errorMessage);
            }

            // Create order entity
            Order order = orderMapper.toEntity(request);
            order.setOrderNumber(orderNumberService.generateOrderNumber());
            order.setStatus(OrderStatus.PENDING);

            // Calculate order items and totals
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

            for (OrderItem item : orderItems) {
                order.addOrderItem(item);
            }

            Order savedOrder = orderRepository.save(order);

            // Publish order created event
            publishOrderCreatedEvent(savedOrder);

            log.info("Order created successfully with ID: {} and number: {}",
                    savedOrder.getOrderId(), savedOrder.getOrderNumber());

            return orderMapper.toDto(savedOrder);


        } catch (Exception e) {
            log.error("Error creating order for customer: {}", request.getCustomerId(), e);
            throw new OrderProcessingException("Failed to create order", e);
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
    public Page<OrderDto> getOrdersByCustomerId(String customerId, Pageable pageable) {
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

        orderStatusService.validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for ID: {}", orderId);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void cancelOrder(UUID orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

            orderStatusService.validateCancellation(order.getStatus());

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            log.info("Order cancelled successfully with ID: {}", orderId);
        } catch (OrderProcessingException e) {
            log.error("Failed to cancel order with ID: {} - {}", orderId, e.getMessage());
            throw e;
        }
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
    public long getOrderCountByCustomerAndStatus(String customerId, OrderStatus status) {
        log.debug("Counting orders for customer: {} with status: {}", customerId, status);

        return orderRepository.countByCustomerIdAndStatus(customerId, status);
    }


    private void publishOrderCreatedEvent(Order order) {
        OrderEventDto orderEvent = createOrderEvent(order, OrderStatus.CONFIRMED);
        orderEventPublisher.publishOrderEvent(orderEvent);
    }

    private void publishOrderCancelledEvent(Order order) {
        OrderEventDto orderEvent = createOrderEvent(order, OrderStatus.CANCELLED);
        orderEventPublisher.publishOrderEvent(orderEvent);
    }



    private OrderEventDto createOrderEvent(Order order, OrderStatus status) {
        return OrderEventDto.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .status(status)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .eventType(status == OrderStatus.CANCELLED ? "ORDER_CANCELLED" : "ORDER_CREATED")
                .build();
    }
}