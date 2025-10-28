package com.nahid.product.aspects;


import com.nahid.product.dto.event.AuditEventMessageDto;
import com.nahid.product.enums.EventStatus;
import com.nahid.product.publisher.AuditEventPublisher;
import com.nahid.product.util.annotation.Auditable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditEventPublisher auditEventPublisher;
    private final HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = null;
        try {
            result = joinPoint.proceed();
            AuditEventMessageDto auditEventMessageDto = buildEventMessageDto(result, auditable, EventStatus.SUCCESS , null);
            auditEventPublisher.publishAuditEvent(auditEventMessageDto);
            return result;
        } catch (Exception e) {
            AuditEventMessageDto auditEventMessageDto = buildEventMessageDto(result, auditable, EventStatus.FAILED, e.getMessage());
            auditEventPublisher.publishAuditEvent(auditEventMessageDto);
            throw e;
        }
    }

    private AuditEventMessageDto buildEventMessageDto(Object result, Auditable auditable,
                                                      EventStatus eventStatus, String errorMessage) {
       return  AuditEventMessageDto.builder()
                .eventId(UUID.randomUUID().toString())
                .entityId(this.extractEntityId(result))
                .userId(this.getUserId())
                .ipAddress(this.getIpAddress() )
                .status(eventStatus)
                .eventType(auditable.eventType())
                .entityType(auditable.entityType())
                .userId(this.getUserId())
                .action(auditable.action())
                .errorMessage(errorMessage)
                .build();
    }

    private String getIpAddress() {

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            return request.getRemoteAddr();
        }
        return ipAddress.split(",")[0].trim();
    }

    private String extractEntityId(Object result) {
        if (result == null) {
            return "";
        }
        try {
            if (result instanceof Long || result instanceof String || result instanceof UUID) {
                return result.toString();
            }
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return id != null ? id.toString() : "";

        } catch (Exception e) {
            log.warn("Could not extract ID from result of type {}", result.getClass().getSimpleName(), e);
            return "";
        }
    }

    private String getUserId() {
        if(request.getHeader("X-Auth-User") != null) {
            return request.getHeader("X-Auth-User");
        }
        return "";
    }




}
