package com.nahid.audit.entity;


import jakarta.persistence.*;
import lombok.Data;


@Data
@Table(name="audit_log")
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(nullable = false, unique = true, length = 100)
    private String eventType;

    @Column(name="entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name="entity_id")
    private String entityId;

    private String actionType;

}
