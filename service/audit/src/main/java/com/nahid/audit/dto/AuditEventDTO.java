package com.nahid.audit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nahid.audit.enums.EventStatus;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventDTO {
    private String eventId;
    private String eventType;
    private String serviceName;
    private String entityName;
    private String entityId;
    private String userId;
    private String action;
    private LocalDateTime timestamp ;
    private String ipAddress;
    private EventStatus status;
    private String errorMessage;
}
