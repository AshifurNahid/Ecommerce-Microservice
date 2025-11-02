package com.nahid.notification.consumer;

import com.nahid.notification.dto.OrderEventDto;
import com.nahid.notification.dto.PaymentNotificationDto;
import com.nahid.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topic.payment-notification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )   public void handlePaymentNotification(
            ConsumerRecord<String, PaymentNotificationDto> record,
            @Payload PaymentNotificationDto paymentNotificationDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received payment notification from topic: {}, partition: {}, offset: {}, paymentId: {}",
                topic, partition, offset, paymentNotificationDto.getPaymentId());

        try {
            if (paymentNotificationDto.getPaymentId() == null) {
                log.error("Payment notification missing required paymentId");
                return;
            }

            if (paymentNotificationDto.getCustomerId() == null || paymentNotificationDto.getCustomerId().isEmpty()) {
                log.error("Payment notification missing required customerId for paymentId: {}",
                        paymentNotificationDto.getPaymentId());
                return;
            }

            notificationService.processPaymentNotification(paymentNotificationDto);
            acknowledgment.acknowledge();

            log.info("Payment notification processed successfully for paymentId: {}",
                    paymentNotificationDto.getPaymentId());

        } catch (Exception e) {
            log.error("Error processing payment notification for paymentId: {}. Error: {}",
                    paymentNotificationDto.getPaymentId(), e.getMessage(), e);

            // In production, you might want to send to DLQ or implement retry logic
            // For now, we'll acknowledge to avoid infinite retries
            acknowledgment.acknowledge();

            // You can also implement custom retry logic here
            // handlePaymentNotificationError(paymentNotificationDto, e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.order-notification}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderKafkaListenerContainerFactory"
    )   public void handleOrderNotification(
            ConsumerRecord<String, OrderEventDto> record,
            @Payload OrderEventDto orderEventDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received order notification from topic: {}, partition: {}, offset: {}, orderId: {}",
                topic, partition, offset, orderEventDto.getOrderId());

        try {
            // Validate required fields
//            if (orderEventDto.getOrderId() == null) {
//                log.error("Order notification missing required orderId");
//                return;
//            }
//
//            if (orderEventDto.getCustomerId() == null || orderEventDto.getCustomerId().isEmpty()) {
//                log.error("Order notification missing required customerId for orderId: {}",
//                        orderEventDto.getOrderId());
//                return;
//            }
//
//            if (orderEventDto.getEventType() == null || orderEventDto.getEventType().isEmpty()) {
//                log.error("Order notification missing required eventType for orderId: {}",
//                        orderEventDto.getOrderId());
//                return;
//            }

            // Process the order notification
            notificationService.processOrderNotification(orderEventDto);

            // Acknowledge the message
            acknowledgment.acknowledge();

            log.info("Order notification processed successfully for orderId: {}",
                    orderEventDto.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order notification for orderId: {}. Error: {}",
                    orderEventDto.getOrderId(), e.getMessage(), e);

            // In production, you might want to send to DLQ or implement retry logic
            // For now, we'll acknowledge to avoid infinite retries
            acknowledgment.acknowledge();

            // You can also implement custom retry logic here
            // handleOrderNotificationError(orderEventDto, e);
        }
    }

    // Optional: Error handling methods for custom retry logic
    private void handlePaymentNotificationError(PaymentNotificationDto paymentNotificationDto, Exception e) {
        log.error("Handling payment notification error for paymentId: {}",
                paymentNotificationDto.getPaymentId());

        // Implement custom error handling logic here
        // Examples:
        // - Send to Dead Letter Queue
        // - Store in error table for manual processing
        // - Send alert to monitoring system
        // - Implement exponential backoff retry
    }

    private void handleOrderNotificationError(OrderEventDto orderEventDto, Exception e) {
        log.error("Handling order notification error for orderId: {}", orderEventDto.getOrderId());

        // Implement custom error handling logic here
        // Examples:
        // - Send to Dead Letter Queue
        // - Store in error table for manual processing
        // - Send alert to monitoring system
        // - Implement exponential backoff retry
    }
}