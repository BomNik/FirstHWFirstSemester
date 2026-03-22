package com.mipt.nikitabumagin.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Cross-cutting aspect that provides method-level logging for all service-layer beans.
 *
 * <p>Uses an {@code @Around} advice to log method entry, exit (including the return value),
 * and any exceptions thrown by methods within the {@code com.mipt.nikitabumagin.service}
 * package.</p>
 *
 * @see org.aspectj.lang.annotation.Aspect
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Logs start/end of all methods in the service package. Also logs returned value; for void
     * methods logs "void".
     */
    @Around("execution(* com.mipt.nikitabumagin.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();

        log.info("[SERVICE] -> {}", signature);
        try {
            Object result = joinPoint.proceed();

            if (joinPoint.getSignature() instanceof MethodSignature methodSignature
                    && methodSignature.getReturnType().equals(Void.TYPE)) {
                log.info("[SERVICE] <- {} result=void", signature);
            } else {
                log.info("[SERVICE] <- {} result={}", signature, result);
            }

            return result;
        } catch (Throwable ex) {
            log.error("[SERVICE] !! {} threw {}: {}", signature, ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }
}
