package com.nahid.product.aspects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;


@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.nahid.product.controller..*)")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = getServletRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : joinPoint.getSignature().toShortString();

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            int status = resolveStatus(result, response, null);
            log.info("Completed {} {} with status {} in {} ms", method, uri, status, System.currentTimeMillis() - startTime);
            return result;
        } catch (Throwable ex) {
            int status = resolveStatus(null, response, ex);
            log.error("Error handling {} {} with status {} in {} ms: {}", method, uri, status,
                    System.currentTimeMillis() - startTime, ex.getMessage(), ex);
            throw ex;
        }
    }

    ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes attributes) {
            return attributes;
        }
        return null;
    }

    int resolveStatus(Object result, HttpServletResponse response, Throwable error) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        if (error instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getStatusCode().value();
        }
        if (response != null && response.getStatus() > 0) {
            return response.getStatus();
        }
        return error == null ? HttpStatus.OK.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
