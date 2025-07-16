package com.authapp.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Log every controller endpoint call with parameters
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Endpoint called: {} with args: {}", method, Arrays.toString(args));
        Object result = joinPoint.proceed();
        logger.info("Endpoint {} returned: {}", method, result);
        return result;
    }

    // Log every method in com.authapp.demo package (excluding getters/setters)
    @Around("execution(* com.authapp.demo.*(..)) " +
            "&& !execution(* get*(..)) " +
            "&& !execution(* set*(..)) " +
            "&& !within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Method invoked: {} with args: {}", method, Arrays.toString(args));
        Object result = joinPoint.proceed();
        logger.info("Method {} returned: {}", method, result);
        return result;
    }
} 