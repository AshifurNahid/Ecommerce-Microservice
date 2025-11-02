package com.nahid.audit.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Table(name="audit_log",indexes = {
        @Index(columnList = "event_id"),
        @Index(columnList = "event_type"),
        @Index(columnList = "user_id")
})
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_id",nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, unique = true, length = 100)
    private String eventType;

    @Column(name="entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name="entity_id")
    private String entityId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String action;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

}
