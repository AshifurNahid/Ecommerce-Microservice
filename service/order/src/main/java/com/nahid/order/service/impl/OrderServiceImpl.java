package com.nahid.order.service.impl;

import com.nahid.order.dto.OrderEventDto;
import com.nahid.order.dto.request.CreateOrderItemRequest;
import com.nahid.order.dto.request.CreateOrderRequest;
import com.nahid.order.dto.request.OrderDto;
import com.nahid.order.dto.response.PurchaseProductItemResultDto;
import com.nahid.order.dto.response.PurchaseProductResponseDto;
import com.nahid.order.entity.Order;
import com.nahid.order.entity.OrderItem;
import com.nahid.order.enums.OrderStatus;
import com.nahid.order.exception.OrderNotFoundException;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.mapper.OrderMapper;
import com.nahid.order.producer.OrderEventPublisher;
import com.nahid.order.repository.OrderRepository;
import com.nahid.order.service.OrderNumberService;
import com.nahid.order.service.OrderService;
import com.nahid.order.service.OrderStatusService;
import com.nahid.order.service.ProductPurchaseService;
import com.nahid.order.service.UserValidationService;
import com.nahid.order.util.annotation.Auditable;
import com.nahid.order.util.constant.ExceptionMessageConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nahid.order.util.constant.AppConstant.ORDER;

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
    @Auditable(eventType = "CREATE", entityName = ORDER, action = "CREATE_ORDER")
    public OrderDto createOrder(CreateOrderRequest request) {
        String orderNumber = null;
        boolean reservationCreated = false;

        try {
            // Validate user
            userValidationService.validateUserForOrder(request.getUserId());

            // Generate order number and reserve products
            orderNumber = orderNumberService.generateOrderNumber();
            PurchaseProductResponseDto reservationResponse = productPurchaseService.reserveProducts(request, orderNumber);

            if (reservationResponse == null || reservationResponse.getItems() == null || reservationResponse.getItems().isEmpty()) {
                throw new OrderProcessingException(String.format(
                        ExceptionMessageConstant.PRODUCT_RESERVATION_FAILED,
                        "Product service returned empty reservation data"));
            }
            reservationCreated = true;

            // Build reserved items map
            Map<Long, PurchaseProductItemResultDto> reservedItemsMap = reservationResponse.getItems()
                    .stream()
                    .collect(Collectors.toMap(
                            PurchaseProductItemResultDto::getProductId,
                            Function.identity(),
                            (existing, replacement) -> existing));

            // Create order entity
            Order order = orderMapper.toEntity(request);
            order.setOrderNumber(orderNumber);
            order.setStatus(OrderStatus.PENDING);

            // Create order items
            List<OrderItem> orderItems = request.getOrderItems().stream()
                    .map(itemRequest -> createOrderItem(itemRequest, reservedItemsMap))
                    .toList();

            // Calculate total and add items
            BigDecimal totalAmount = orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(totalAmount);
            orderItems.forEach(order::addOrderItem);

            // Save and confirm
            Order savedOrder = orderRepository.save(order);
            productPurchaseService.confirmReservation(orderNumber);

            publishOrderEvent(savedOrder, OrderStatus.PENDING);

            return orderMapper.toDto(savedOrder);

        } catch (OrderProcessingException e) {
            rollbackReservation(reservationCreated, orderNumber);
            throw e;
        } catch (Exception e) {
            rollbackReservation(reservationCreated, orderNumber);
            throw new OrderProcessingException(
                    String.format(ExceptionMessageConstant.ORDER_CREATION_FAILED, e.getMessage()), e);
        }
    }

    private OrderItem createOrderItem(CreateOrderItemRequest itemRequest,
                                      Map<Long, PurchaseProductItemResultDto> reservedItemsMap) {
        Long productId = itemRequest.getProductId();
        PurchaseProductItemResultDto reservedItem = reservedItemsMap.get(productId);

        // Validate reserved item
        if (reservedItem == null || !reservedItem.isAvailable()) {
            throw new OrderProcessingException(String.format(
                    ExceptionMessageConstant.PRODUCT_RESERVATION_FAILED,
                    "Product unavailable or not reserved: " + productId));
        }

        if (reservedItem.getPrice() == null) {
            throw new OrderProcessingException(String.format(
                    ExceptionMessageConstant.PRODUCT_PRICE_FETCH_FAILED,
                    "Price missing for product: " + productId));
        }

        // Log quantity mismatch if any
        if (!itemRequest.getQuantity().equals(reservedItem.getRequestedQuantity())) {
            log.warn("Quantity mismatch for product {}. Requested: {}, Reserved: {}",
                    productId, itemRequest.getQuantity(), reservedItem.getRequestedQuantity());
        }

        // Create order item
        OrderItem item = orderMapper.toEntity(itemRequest);
        item.setProductName(reservedItem.getProductName());
        item.setProductSku(reservedItem.getSku());
        item.setUnitPrice(reservedItem.getPrice());
        item.setTotalPrice(reservedItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        return item;
    }

    private void rollbackReservation(boolean reservationCreated, String orderNumber) {
        if (reservationCreated && orderNumber != null) {
            try {
                productPurchaseService.releaseReservation(orderNumber);
            } catch (Exception e) {
                log.error("Failed to release reservation for order {}: {}",
                        orderNumber, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format(ExceptionMessageConstant.ORDER_NOT_FOUND_BY_NUMBER, orderNumber)));
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
    @Auditable(eventType = "UPDATE", entityName = ORDER, action = "UPDATE_ORDER_STATUS")
    public OrderDto updateOrderStatus(UUID orderId, OrderStatus status) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(
                            String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));

            orderStatusService.validateStatusTransition(order.getStatus(), status);
            order.setStatus(status);

            Order savedOrder = orderRepository.save(order);
            return orderMapper.toDto(savedOrder);
        } catch (OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(
                    String.format(ExceptionMessageConstant.ORDER_UPDATE_FAILED, e.getMessage()), e);
        }
    }

    @Override
    @Auditable(eventType = "UPDATE", entityName = ORDER, action = "CANCEL_ORDER")
    public void cancelOrder(UUID orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException(
                            String.format(ExceptionMessageConstant.ORDER_NOT_FOUND, orderId)));

            orderStatusService.validateCancellation(order.getStatus());

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            publishOrderEvent(order, OrderStatus.CANCELLED);

        } catch (OrderProcessingException | OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(
                    String.format(ExceptionMessageConstant.ORDER_CANCELLATION_FAILED, e.getMessage()), e);
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

    private void publishOrderEvent(Order order, OrderStatus status) {
        try {
            OrderEventDto orderEvent = OrderEventDto.builder()
                    .orderId(order.getOrderId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .status(status)
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .eventType("ORDER_" + status.name())
                    .build();

            orderEventPublisher.publishOrderEvent(orderEvent);
        } catch (Exception e) {
            log.error("Failed to publish order event for orderId {}: {}",
                    order.getOrderId(), e.getMessage());
        }
    }
}