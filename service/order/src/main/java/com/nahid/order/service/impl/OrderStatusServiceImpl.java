package com.nahid.order.service.impl;

import com.nahid.order.enums.OrderStatus;
import com.nahid.order.exception.OrderProcessingException;
import com.nahid.order.service.OrderStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderStatusServiceImpl implements OrderStatusService {

    @Override
    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
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

    @Override
    public void validateCancellation(OrderStatus currentStatus) {
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            throw new OrderProcessingException("Cannot cancel order with status: " + currentStatus);
        }
    }
}

