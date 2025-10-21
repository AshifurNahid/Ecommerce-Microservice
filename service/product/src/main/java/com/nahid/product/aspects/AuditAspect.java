package com.nahid.product.aspects;


import com.nahid.product.util.annotation.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable){
        Instant timeStamp= Instant.now();
        try {

        } catch (Exception e) {

        }
        return null;

    }


}
