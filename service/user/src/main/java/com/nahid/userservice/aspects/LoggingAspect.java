package com.nahid.userservice.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("within(com.nahid.userservice.controller..*) ")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        HttpServletRequest request = getCurrentRequest();

        assert request != null;
        String httpMethod = StringUtils.defaultIfBlank(request.getMethod(), "N/A");
        String requestUri = StringUtils.defaultIfBlank(request.getRequestURI(), "N/A");

        log.info("➡ {} {} | Entering {} with args {}", httpMethod, requestUri, methodName, args);

        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - start;

            log.info("➡ {} {} | Exiting {} | ExecutionTime={}ms", httpMethod, requestUri, methodName, timeTaken);

            return result;
        } catch (Exception e) {
            log.error("❌ {} {} | Exception in {} | Message={}", httpMethod, requestUri, methodName, e.getMessage(), e);
            throw e;
        }
    }

//    @Around("@annotation(com.sil.userservice.util.annotation.LogThis)")
//    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//
//        String methodName = joinPoint.getSignature().toShortString();
//        Object[] args = joinPoint.getArgs();
//
//        log.info("➡ Entering {} with args {}", methodName, args);
//
//        try {
//            Object result = joinPoint.proceed();
//            long timeTaken = System.currentTimeMillis() - start;
//
//            log.info("⬅ Exiting {} | Returned={} | ExecutionTime={}ms",
//                    methodName, result, timeTaken);
//
//            return result;
//        } catch (Exception e) {
//            log.error("❌ Exception in {} | Message={}",
//                    methodName, e.getMessage(), e);
//            throw e;
//        }
//    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attrs != null) ? attrs.getRequest() : null;
    }

}
