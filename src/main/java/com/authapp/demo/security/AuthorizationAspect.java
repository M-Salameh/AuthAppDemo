package com.authapp.demo.security;

import com.authapp.demo.entity.User;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
            if (arg instanceof String) {
                username = (String) arg;
            } else if (arg instanceof Long) {
                userId = (Long) arg;
            }
        }
        if (username == null && userId == null){
            throw new RuntimeException("Username or userId not found in arguments");
        }
        // If username is not found, try to get it from userId (if available)
        if (username == null && userId != null) {
            username = userRepository.findById(userId).map(User::getUsername).orElse(null);
        }
        if (!JwtUtil.isAdmin(authHeader) && (username == null || !JwtUtil.isSelf(authHeader, username))) {
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