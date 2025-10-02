package com.nahid.order.service.impl;

import com.nahid.order.dto.*;
import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.OrderDto;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import com.nahid.order.entity.Order;
import com.nahid.order.entity.OrderItem;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.exception.OrderNotFoundException;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.mapper.OrderMapper;
import com.nahid.order.producer.OrderEventPublisher;
import com.nahid.order.repository.OrderRepository;
import com.nahid.order.service.*;
import com.nahid.order.util.constant.ExceptionMessageConstant;
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
    private final UserValidationService userValidationService;
    private final ProductPurchaseService productPurchaseService;
    private final OrderStatusService orderStatusService;
    private final OrderNumberService orderNumberService;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {

        try {
            userValidationService.validateUserForOrder(request.getUserId());

            String orderNumber = orderNumberService.generateOrderNumber();

            PurchaseProductResponseDto purchaseResponse = productPurchaseService.purchaseProducts(request, orderNumber);

            if (purchaseResponse == null || !purchaseResponse.isSuccess()) {
                String errorMessage = productPurchaseService.formatPurchaseError(purchaseResponse);
                throw new OrderProcessingException(String.format(ExceptionMessageConstant.PRODUCT_PURCHASE_FAILED, errorMessage));
            }
            Order order = orderMapper.toEntity(request);
            order.setOrderNumber(orderNumber);
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

            for (OrderItem item : orderItems) {
                order.addOrderItem(item);
            }

            Order savedOrder = orderRepository.save(order);
            publishOrderCreatedEvent(savedOrder);
            return orderMapper.toDto(savedOrder);

        } catch (OrderProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(String.format(ExceptionMessageConstant.ORDER_CREATION_FAILED, e.getMessage()), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(String.format(ExceptionMessageConstant.ORDER_NOT_FOUND_BY_NUMBER, orderNumber)));

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus status) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));
            orderStatusService.validateStatusTransition(order.getStatus(), status);
            order.setStatus(status);
            Order savedOrder = orderRepository.save(order);
            return orderMapper.toDto(savedOrder);
        } catch (Exception e) {

            throw new OrderProcessingException(String.format(ExceptionMessageConstant.ORDER_UPDATE_FAILED, e.getMessage()), e);
        }
    }

    @Override
    public void cancelOrder(UUID orderId) {

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));

            orderStatusService.validateCancellation(order.getStatus());

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

        } catch (OrderProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(String.format(ExceptionMessageConstant.ORDER_CANCELLATION_FAILED, e.getMessage()), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {

        return orderRepository.findByStatusOrderByCreatedAtDesc(status, Pageable.unpaged())
                .getContent()
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getOrderCountByUserAndStatus(Long userId, OrderStatus status) {
        return orderRepository.countByUserIdAndStatus(userId, status);
    }

    private void publishOrderCreatedEvent(Order order) {
        try {
            OrderEventDto orderEvent = createOrderEvent(order, OrderStatus.CONFIRMED);
            orderEventPublisher.publishOrderEvent(orderEvent);
        } catch (Exception e) {
            log.error("Failed to publish order created event for orderId {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void publishOrderCancelledEvent(Order order) {
        try {
            OrderEventDto orderEvent = createOrderEvent(order, OrderStatus.CANCELLED);
            orderEventPublisher.publishOrderEvent(orderEvent);
        } catch (Exception ignored) {
        }
    }


    private OrderEventDto createOrderEvent(Order order, OrderStatus status) {
        return OrderEventDto.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(status)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .eventType(status == OrderStatus.CANCELLED ? "ORDER_CANCELLED" : "ORDER_CREATED")
                .build();
    }
}