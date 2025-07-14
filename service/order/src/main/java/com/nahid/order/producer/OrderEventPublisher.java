package com.nahid.order.producer;

import com.nahid.order.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Slf4j

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEventDto> kafkaTemplate;

    @Value("${kafka.topic.order-notification}")
    private String orderNotificationTopic;

    public void publishOrderEvent(OrderEventDto orderEvent) {

        try {

            if (orderEvent == null) {
                log.error("Order event is null, cannot publish.");
                return;
            }
            if (orderEvent.getOrderId() == null || orderEvent.getOrderNumber() == null) {
                log.error("Order event is missing required fields: {}", orderEvent);
                return;
            }


            Message<OrderEventDto> message = MessageBuilder
                    .withPayload(orderEvent)
                    .setHeader(KafkaHeaders.TOPIC, orderNotificationTopic)
                    .setHeader(KafkaHeaders.KEY, orderEvent.getOrderId().toString())
                    .build();

            CompletableFuture<SendResult<String, OrderEventDto>> future = kafkaTemplate.send(message);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send order event: {}", ex.getMessage());
                } else {
                    log.info("Order event sent successfully: {}", result.getProducerRecord().value());
                }
            });
        }
        catch (Exception e) {
            log.error("Error publishing order event: {}", e.getMessage(), e);
        }

    }



}
