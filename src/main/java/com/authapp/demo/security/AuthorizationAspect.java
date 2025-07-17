package com.authapp.demo.security;

import com.authapp.demo.entity.User;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthorizationAspect {
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    @Around("@annotation(com.authapp.demo.security.AdminOnly)")
    public Object checkAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        String authHeader = getAuthHeader();
        if (!JwtUtil.isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(com.authapp.demo.security.SelfOrAdmin)")
    public Object checkSelfOrAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        String authHeader = getAuthHeader();
        Object[] args = joinPoint.getArgs();
        String username = null;
        Long userId = null;
        // Try to extract username or userId from arguments
        for (Object arg : args) {
             if (arg instanceof Long) {
                userId = (Long) arg;
            }
        }
        if (userId == null){
            logger.error("Username or userId not found in arguments");
            throw new RuntimeException("Username or userId not found in arguments");
        }
        // If username is not found, try to get it from userId (if available)
        if (userId != null) {
            logger.warn("username is null and id not null = {}", userId);
            username = userRepository.findById(userId).map(User::getUsername).orElse(null);
            logger.warn("username fetched from db for id = {} is {}", userId, username);
        }
        if (!JwtUtil.isAdmin(authHeader) &&  !JwtUtil.isSelf(authHeader, username)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        return joinPoint.proceed();
    }

    private String getAuthHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            return request.getHeader("Authorization");
        }
        throw new RuntimeException("Request AuthZ attributes not found");
    }
} 