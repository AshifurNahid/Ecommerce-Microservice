package com.nahid.product.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nahid.product.enums.EventStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventMessageDto {
    private String eventId;
    private String eventType; // CREATE, UPDATE, DELETE, ACCESS, LOGIN, etc.
    private String serviceName; // user-service, order-service, etc.
    private String entityName; // User, Order, Product, etc.
    private String entityId;
    private String userId;
    private String action;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String ipAddress;
    private EventStatus status;
    private String errorMessage;


}
