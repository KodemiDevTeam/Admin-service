package com.example.admin_service.component;

import com.example.admin_service.dto.AdminResponseDTO;
import com.example.admin_service.feign.UserClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private final UserClient userClient;

    public RoleCheckAspect(UserClient userClient) {
        this.userClient = userClient;
    }

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String role = extractUserFromToken(token).get("role", String.class);

        if (role != null && Arrays.asList(requiresRole.value()).contains(role)) {
            return joinPoint.proceed();  // ✅ Allowed
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"); // ❌ Blocked
    }

    @Around("@annotation(requiresSuperRole)")
    public Object checkSuperRole(ProceedingJoinPoint joinPoint, RequiresSuperRole requiresSuperRole) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String userId = extractUserFromToken(token).get("userId", String.class);

        String role = userClient.getAdmin(userId).getAdminRole();
        if (role != null && Arrays.asList(requiresSuperRole.value()).contains(role)) {
            return joinPoint.proceed();  // ✅ Allowed
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"); // ❌ Blocked
    }

    private Claims extractUserFromToken(String token) {
        try {
            byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            return claims;
        } catch (Exception e) {
            return null;  // expired / invalid token
        }
    }
}